package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.adapter;

import com.appbit.geoanalytics.application.antenna.in.AntennaIngestResult;
import com.appbit.geoanalytics.application.antenna.in.IngestAntennasUseCase;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.csv.AntennaCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.AntennaEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.AntennaJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestAntennasService implements IngestAntennasUseCase {

    private static final String REGION_CODE_PREFIX = "REG_";
    private static final BigDecimal MIN_LAT = new BigDecimal("-90");
    private static final BigDecimal MAX_LAT = new BigDecimal("90");
    private static final BigDecimal MIN_LON = new BigDecimal("-180");
    private static final BigDecimal MAX_LON = new BigDecimal("180");

    private final DatasetObjectStoragePort storagePort;
    private final DataSourcePort dataSourcePort;
    private final GenericCsvReader csvReader;
    private final AntennaJpaRepository antennaRepository;
    private final RegionJpaRepository regionRepository;
    private final IngestionLifecycleManager lifecycleManager;
    private final TransactionTemplate transactionTemplate;
    private final IdGeneratorPort idGeneratorPort;

    @Override
    public AntennaIngestResult execute(DatasetObjectKey key) {
        var sourceId = resolveSourceId(key.value());
        var ingestionRun = lifecycleManager.start(key.value(), sourceId);

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
            var antennasToInsert = new ArrayList<AntennaEntity>();
            var rejected = 0;
            var readCount = 0;

            while (iterator.hasNext()) {
                readCount++;
                var entity = processRow(iterator.next(), processedEcgis, sourceId);

                if (entity != null) {
                    antennasToInsert.add(entity);
                } else {
                    rejected++;
                }
            }

            antennasToInsert.forEach(antenna ->
                    antennaRepository.insertIgnoreConflict(
                            antenna.getId(), antenna.getEcgi(), antenna.getRegionId(),
                            antenna.getClusterName(), antenna.getMunicipality(),
                            antenna.getLatitude(), antenna.getLongitude(),
                            antenna.getSourceId(), antenna.getCreatedAt()
                    )
            );

            return AntennaIngestResult.of(readCount, antennasToInsert.size(), rejected);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest CSV stream: " + e.getMessage(), e);
        }
    }

    private AntennaEntity processRow(AntennaCsvRow row, Set<String> processed, UUID sourceId) {
        if (!isValidRow(row)) return null;

        var ecgi = row.ecgi().trim();
        if (!processed.add(ecgi)) return null;

        var region = findOrCreateRegion(row);
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

    private RegionEntity findOrCreateRegion(AntennaCsvRow row) {
        var cluster = row.cluster().trim();
        var municipio = row.municipio().trim();

        return regionRepository.findByClusterNameAndMunicipality(cluster, municipio)
                .orElseGet(() -> regionRepository.save(RegionEntity.builder()
                        .id(idGeneratorPort.generate())
                        .regionCode(REGION_CODE_PREFIX + cluster.toUpperCase())
                        .regionName(cluster)
                        .clusterName(cluster)
                        .municipality(municipio)
                        .centerLatitude(new BigDecimal(row.lat().trim()))
                        .centerLongitude(new BigDecimal(row.lon().trim()))
                        .createdAt(Instant.now())
                        .build()));
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

    private UUID resolveSourceId(String fileName) {
        return dataSourcePort.findByFileName(new SourceFileName(fileName))
                .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + fileName))
                .id();
    }
}
