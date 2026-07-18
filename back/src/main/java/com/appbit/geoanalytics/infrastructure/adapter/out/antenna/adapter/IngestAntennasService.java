package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.adapter;

import com.appbit.geoanalytics.application.antenna.in.AntennaIngestResult;
import com.appbit.geoanalytics.application.antenna.in.IngestAntennasUseCase;
import com.appbit.geoanalytics.application.ingestion.in.CsvIngestService;
import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.region.vo.RegionCode;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.csv.AntennaCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.AntennaEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IngestAntennasService implements IngestAntennasUseCase, CsvIngestService {

    private static final String REGION_CODE_PREFIX = "REG_";
    private static final BigDecimal MIN_LAT = new BigDecimal("-90");
    private static final BigDecimal MAX_LAT = new BigDecimal("90");
    private static final BigDecimal MIN_LON = new BigDecimal("-180");
    private static final BigDecimal MAX_LON = new BigDecimal("180");

    private final DatasetObjectStoragePort storagePort;
    private final DataSourcePort dataSourcePort;
    private final GenericCsvReader csvReader;
    private final RegionJpaRepository regionRepository;
    private final IngestionLifecycleManager lifecycleManager;
    private final TransactionTemplate transactionTemplate;
    private final IdGeneratorPort idGeneratorPort;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public AntennaIngestResult execute(DatasetObjectKey key) {
        var sourceId = resolveSourceId(key.value());
        var ingestionRun = lifecycleManager.start(key.value(), sourceId);

        if (ingestionRun == null) {
            return AntennaIngestResult.of(0, 0, 0);
        }

        try {
            var result = transactionTemplate.execute(_ -> ingest(key, sourceId));
            lifecycleManager.complete(ingestionRun, result.rowsRead(), result.rowsInserted(), result.rowsRejected());
            return result;
        } catch (RuntimeException e) {
            lifecycleManager.fail(ingestionRun, e);
            throw e;
        }
    }

    private AntennaIngestResult ingest(DatasetObjectKey key, UUID sourceId) {
        try (var inputStream = storagePort.openStream(key);
             var iterator = csvReader.read(inputStream, AntennaCsvRow.class)) {

            var processedEcgis = new HashSet<String>();
            var regionCache = new HashMap<String, RegionEntity>();
            var antennasToInsert = new ArrayList<AntennaEntity>();
            var rejected = 0;
            var readCount = 0;

            while (iterator.hasNext()) {
                readCount++;
                var entity = processRow(iterator.next(), processedEcgis, regionCache, sourceId);

                if (entity != null) {
                    antennasToInsert.add(entity);
                } else {
                    rejected++;
                }
            }

            batchInsertAntennas(antennasToInsert);

            return AntennaIngestResult.of(readCount, antennasToInsert.size(), rejected);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest CSV stream: " + e.getMessage(), e);
        }
    }

    private void batchInsertAntennas(List<AntennaEntity> antennas) {
        String sql = """
            INSERT INTO antennas (id, ecgi, region_id, cluster_name, municipality, latitude, longitude, source_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (ecgi) DO NOTHING
            """;

        int batchSize = 500;
        for (int i = 0; i < antennas.size(); i += batchSize) {
            List<AntennaEntity> batch = antennas.subList(i, Math.min(i + batchSize, antennas.size()));
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int idx) throws java.sql.SQLException {
                    AntennaEntity antenna = batch.get(idx);
                    ps.setObject(1, antenna.getId());
                    ps.setString(2, antenna.getEcgi());
                    ps.setObject(3, antenna.getRegionId());
                    ps.setString(4, antenna.getClusterName());
                    ps.setString(5, antenna.getMunicipality());
                    ps.setBigDecimal(6, antenna.getLatitude());
                    ps.setBigDecimal(7, antenna.getLongitude());
                    ps.setObject(8, antenna.getSourceId());
                    ps.setTimestamp(9, Timestamp.from(antenna.getCreatedAt()));
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private AntennaEntity processRow(AntennaCsvRow row, Set<String> processed, Map<String, RegionEntity> regionCache, UUID sourceId) {
        if (!isValidRow(row)) return null;

        var ecgi = row.ecgi().trim();
        if (!processed.add(ecgi)) return null;

        var region = findOrCreateRegion(row, regionCache);
        return createAntenna(row, region, sourceId);
    }

    private boolean isValidRow(AntennaCsvRow row) {
        var ecgi = row.ecgi().trim();
        if (ecgi.length() < 12 || ecgi.length() > 16 || !ecgi.chars().allMatch(Character::isDigit)) return false;

        try {
            var lat = new BigDecimal(row.lat().trim());
            var lon = new BigDecimal(row.lon().trim());

            if (lat.compareTo(MIN_LAT) < 0 || lat.compareTo(MAX_LAT) > 0) return false;
            if (lon.compareTo(MIN_LON) < 0 || lon.compareTo(MAX_LON) > 0) return false;
            if (lat.compareTo(BigDecimal.ZERO) == 0 && lon.compareTo(BigDecimal.ZERO) == 0) return false;
        } catch (RuntimeException _) {
            return false;
        }

        return !row.cluster().isBlank() && !row.municipio().isBlank();
    }

    private RegionEntity findOrCreateRegion(AntennaCsvRow row, Map<String, RegionEntity> regionCache) {
        var cluster = row.cluster().trim();
        var municipio = row.municipio().trim();
        var regionCode = new RegionCode(REGION_CODE_PREFIX + cluster).value();

        var cached = regionCache.get(regionCode);
        if (cached != null) return cached;

        var existing = regionRepository.findByRegionCode(regionCode);
        if (existing.isPresent()) {
            var region = existing.get();
            regionCache.put(regionCode, region);
            return region;
        }

        var id = idGeneratorPort.generate();
        regionRepository.insertIgnoreConflict(
                id, regionCode, cluster, cluster, municipio,
                new BigDecimal(row.lat().trim()), new BigDecimal(row.lon().trim()), Instant.now()
        );

        var region = regionRepository.findByRegionCode(regionCode)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve region after insert: " + regionCode));

        regionCache.put(regionCode, region);
        return region;
    }

    private AntennaEntity createAntenna(AntennaCsvRow row, RegionEntity region, UUID sourceId) {
        return AntennaEntity.builder()
                .id(idGeneratorPort.generate())
                .ecgi(row.ecgi().trim())
                .regionId(region.getId())
                .clusterName(row.cluster().trim())
                .municipality(row.municipio().trim())
                .latitude(new BigDecimal(row.lat().trim()))
                .longitude(new BigDecimal(row.lon().trim()))
                .sourceId(sourceId)
                .createdAt(Instant.now())
                .build();
    }

    @Override
    public String supportedFileName() {
        return "antenas_flp.csv";
    }

    @Override
    public CsvIngestResult ingest(DatasetObjectKey key) {
        var result = execute(key);
        return CsvIngestResult.of(result.rowsRead(), result.rowsInserted(), result.rowsRejected());
    }

    private UUID resolveSourceId(String fileName) {
        return dataSourcePort.findByFileName(new SourceFileName(fileName))
                .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + fileName))
                .id();
    }
}
