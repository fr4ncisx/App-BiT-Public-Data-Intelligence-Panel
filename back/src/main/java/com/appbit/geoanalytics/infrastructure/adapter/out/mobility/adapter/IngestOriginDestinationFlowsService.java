package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.adapter;

import com.appbit.geoanalytics.application.ingestion.in.CsvIngestService;
import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv.OriginDestinationFlowCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.OriginDestinationFlowEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngestOriginDestinationFlowsService implements CsvIngestService {

    private static final Set<String> VALID_PERIODS = Set.of("MADRUGADA", "MANHA", "TARDE", "NOITE");

    private final DatasetObjectStoragePort storagePort;
    private final DataSourcePort dataSourcePort;
    private final GenericCsvReader csvReader;
    private final RegionJpaRepository regionRepository;
    private final IngestionLifecycleManager lifecycleManager;
    private final TransactionTemplate transactionTemplate;
    private final IdGeneratorPort idGeneratorPort;
    private final JdbcTemplate jdbcTemplate;
    private final IngestionProperties ingestionProperties;

    @Override
    public String supportedFileName() {
        return "tensor_od.csv";
    }

    @Override
    public CsvIngestResult ingest(DatasetObjectKey key) {
        var sourceId = resolveSourceId(key.value());
        var ingestionRun = lifecycleManager.start(key.value(), sourceId);

        if (ingestionRun == null) {
            return new CsvIngestResult(0, 0, 0);
        }

        try {
            var result = transactionTemplate.execute(_ -> doIngest(key, sourceId));
            lifecycleManager.complete(ingestionRun, result.rowsRead(), result.rowsInserted(), result.rowsRejected());
            return result;
        } catch (RuntimeException e) {
            lifecycleManager.fail(ingestionRun, e);
            throw e;
        }
    }

    private CsvIngestResult doIngest(DatasetObjectKey key, UUID sourceId) {
        try (var inputStream = storagePort.openStream(key);
             var iterator = csvReader.read(inputStream, OriginDestinationFlowCsvRow.class)) {

            var rejected = 0;
            var readCount = 0;
            var processedKeys = new HashSet<String>();
            var flowsToInsert = new ArrayList<OriginDestinationFlowEntity>();

            while (iterator.hasNext()) {
                readCount++;
                var entity = processRow(iterator.next(), processedKeys, sourceId);
                if (entity != null) {
                    flowsToInsert.add(entity);
                } else {
                    rejected++;
                }
            }

            batchInsertOdFlows(flowsToInsert);

            return new CsvIngestResult(readCount, flowsToInsert.size(), rejected);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest OD flows CSV: " + e.getMessage(), e);
        }
    }

    private void batchInsertOdFlows(List<OriginDestinationFlowEntity> flows) {
        String sql = """
            INSERT INTO origin_destination_flows (
                id, source_id, origin_region_id, destination_region_id,
                origin_cluster_name, destination_cluster_name, origin_municipality, destination_municipality,
                origin_latitude, origin_longitude, destination_latitude, destination_longitude,
                predominant_period, same_cluster, users_count, trips_count, average_distance_km, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (source_id, origin_region_id, destination_region_id, predominant_period) DO NOTHING
            """;

        int batchSize = ingestionProperties.batchSize();
        for (int i = 0; i < flows.size(); i += batchSize) {
            List<OriginDestinationFlowEntity> batch = flows.subList(i, Math.min(i + batchSize, flows.size()));
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int idx) throws java.sql.SQLException {
                    OriginDestinationFlowEntity f = batch.get(idx);
                    ps.setObject(1, f.getId());
                    ps.setObject(2, f.getSourceId());
                    ps.setObject(3, f.getOriginRegionId());
                    ps.setObject(4, f.getDestinationRegionId());
                    ps.setString(5, f.getOriginClusterName());
                    ps.setString(6, f.getDestinationClusterName());
                    ps.setString(7, f.getOriginMunicipality());
                    ps.setString(8, f.getDestinationMunicipality());
                    ps.setBigDecimal(9, f.getOriginLatitude());
                    ps.setBigDecimal(10, f.getOriginLongitude());
                    ps.setBigDecimal(11, f.getDestinationLatitude());
                    ps.setBigDecimal(12, f.getDestinationLongitude());
                    ps.setString(13, f.getPredominantPeriod());
                    ps.setBoolean(14, f.getSameCluster());
                    ps.setLong(15, f.getUsersCount());
                    ps.setLong(16, f.getTripsCount());
                    ps.setBigDecimal(17, f.getAverageDistanceKm());
                    ps.setTimestamp(18, Timestamp.from(f.getCreatedAt()));
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private OriginDestinationFlowEntity processRow(OriginDestinationFlowCsvRow row, Set<String> processedKeys, UUID sourceId) {
        var dedupKey = row.originCluster().trim() + "|" + row.destCluster().trim() + "|" + row.periodo().trim().toUpperCase();
        if (!processedKeys.add(dedupKey)) {
            log.warn("Rejected duplicate: {}", dedupKey);
            return null;
        }

        try {
            var periodo = row.periodo().trim().toUpperCase();
            if (!VALID_PERIODS.contains(periodo)) {
                log.warn("Rejected: invalid period {}", periodo);
                return null;
            }

            var originRegion = regionRepository.findByClusterName(row.originCluster().trim());
            var destRegion = regionRepository.findByClusterName(row.destCluster().trim());
            if (originRegion.isEmpty() || destRegion.isEmpty()) {
                log.warn("Rejected: region not found for cluster {} / {}", row.originCluster(), row.destCluster());
                return null;
            }

            return OriginDestinationFlowEntity.builder()
                    .id(idGeneratorPort.generate())
                    .sourceId(sourceId)
                    .originRegionId(originRegion.get().getId())
                    .destinationRegionId(destRegion.get().getId())
                    .originClusterName(row.originCluster().trim())
                    .destinationClusterName(row.destCluster().trim())
                    .originMunicipality(originRegion.get().getMunicipality())
                    .destinationMunicipality(destRegion.get().getMunicipality())
                    .originLatitude(new BigDecimal(row.originLat().trim()))
                    .originLongitude(new BigDecimal(row.originLon().trim()))
                    .destinationLatitude(new BigDecimal(row.destLat().trim()))
                    .destinationLongitude(new BigDecimal(row.destLon().trim()))
                    .sameCluster("true".equalsIgnoreCase(row.mesmaCluster().trim()))
                    .usersCount(Long.parseLong(row.nUsuarios().trim()))
                    .tripsCount(Long.parseLong(row.nViagens().trim()))
                    .averageDistanceKm(new BigDecimal(row.distanciaMediaKm().trim()).setScale(3, RoundingMode.HALF_UP))
                    .predominantPeriod(periodo)
                    .createdAt(Instant.now())
                    .build();

        } catch (RuntimeException e) {
            log.warn("Rejected row: {}", e.getMessage());
            return null;
        }
    }

    private UUID resolveSourceId(String fileName) {
        return dataSourcePort.findByFileName(new SourceFileName(fileName))
                .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + fileName))
                .id();
    }
}
