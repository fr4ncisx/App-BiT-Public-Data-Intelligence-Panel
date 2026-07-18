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
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv.TravelDistanceMetricCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.TravelDistanceMetricEntity;
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
public class IngestTravelDistanceMetricsService implements CsvIngestService {

    private static final Set<String> VALID_PERIODS = Set.of("MADRUGADA", "MANHA", "TARDE", "NOITE");

    private final DatasetObjectStoragePort storagePort;
    private final DataSourcePort dataSourcePort;
    private final GenericCsvReader csvReader;
    private final RegionJpaRepository regionRepository;
    private final IngestionLifecycleManager lifecycleManager;
    private final TransactionTemplate transactionTemplate;
    private final IdGeneratorPort idGeneratorPort;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public String supportedFileName() {
        return "tensor_tempo_deslocamento.csv";
    }

    @Override
    public CsvIngestResult ingest(DatasetObjectKey key) {
        var sourceId = resolveSourceId(key.value());
        var ingestionRun = lifecycleManager.start(key.value(), sourceId);

        if (ingestionRun == null) {
            return CsvIngestResult.of(0, 0, 0);
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
             var iterator = csvReader.read(inputStream, TravelDistanceMetricCsvRow.class)) {

            var rejected = 0;
            var readCount = 0;
            var processedKeys = new HashSet<String>();
            var metricsToInsert = new ArrayList<TravelDistanceMetricEntity>();

            while (iterator.hasNext()) {
                readCount++;
                var entity = processRow(iterator.next(), processedKeys, sourceId);
                if (entity != null) {
                    metricsToInsert.add(entity);
                } else {
                    rejected++;
                }
            }

            batchInsertTravelDistances(metricsToInsert);

            return CsvIngestResult.of(readCount, metricsToInsert.size(), rejected);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest travel distance metrics CSV: " + e.getMessage(), e);
        }
    }

    private void batchInsertTravelDistances(List<TravelDistanceMetricEntity> metrics) {
        String sql = """
            INSERT INTO travel_distance_metrics (
                id, source_id, origin_region_id, destination_region_id,
                origin_cluster_name, destination_cluster_name, predominant_period,
                same_cluster, observations, average_distance_km, p25_distance_km, p75_distance_km, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (source_id, origin_region_id, destination_region_id, predominant_period) DO NOTHING
            """;

        int batchSize = 500;
        for (int i = 0; i < metrics.size(); i += batchSize) {
            List<TravelDistanceMetricEntity> batch = metrics.subList(i, Math.min(i + batchSize, metrics.size()));
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int idx) throws java.sql.SQLException {
                    TravelDistanceMetricEntity m = batch.get(idx);
                    ps.setObject(1, m.getId());
                    ps.setObject(2, m.getSourceId());
                    ps.setObject(3, m.getOriginRegionId());
                    ps.setObject(4, m.getDestinationRegionId());
                    ps.setString(5, m.getOriginClusterName());
                    ps.setString(6, m.getDestinationClusterName());
                    ps.setString(7, m.getPredominantPeriod());
                    ps.setBoolean(8, m.getSameCluster());
                    ps.setLong(9, m.getObservations());
                    ps.setBigDecimal(10, m.getAverageDistanceKm());
                    ps.setBigDecimal(11, m.getP25DistanceKm());
                    ps.setBigDecimal(12, m.getP75DistanceKm());
                    ps.setTimestamp(13, Timestamp.from(m.getCreatedAt()));
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private TravelDistanceMetricEntity processRow(TravelDistanceMetricCsvRow row, Set<String> processedKeys, UUID sourceId) {
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

            return TravelDistanceMetricEntity.builder()
                    .id(idGeneratorPort.generate())
                    .sourceId(sourceId)
                    .originRegionId(originRegion.get().getId())
                    .destinationRegionId(destRegion.get().getId())
                    .originClusterName(row.originCluster().trim())
                    .destinationClusterName(row.destCluster().trim())
                    .sameCluster("true".equalsIgnoreCase(row.mesmaCluster().trim()))
                    .observations(Long.parseLong(row.nObservacoes().trim()))
                    .averageDistanceKm(new BigDecimal(row.distanciaMediaKm().trim()).setScale(3, RoundingMode.HALF_UP))
                    .p25DistanceKm(new BigDecimal(row.p25DistanciaKm().trim()).setScale(3, RoundingMode.HALF_UP))
                    .p75DistanceKm(new BigDecimal(row.p75DistanciaKm().trim()).setScale(3, RoundingMode.HALF_UP))
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
