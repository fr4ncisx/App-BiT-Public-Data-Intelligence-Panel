package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.adapter;

import com.appbit.geoanalytics.application.ingestion.in.CsvIngestService;
import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.CsvBatchIngester;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.RegionIndex;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.RegionResolver;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv.TravelDistanceMetricCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.TravelDistanceMetricEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngestTravelDistanceMetricsService implements CsvIngestService {

    private static final Set<String> VALID_PERIODS = Set.of("MADRUGADA", "MANHA", "TARDE", "NOITE");

    private static final String INSERT_SQL = """
            INSERT INTO travel_distance_metrics (
                id, source_id, origin_region_id, destination_region_id,
                origin_cluster_name, destination_cluster_name, predominant_period,
                same_cluster, observations, average_distance_km, p25_distance_km, p75_distance_km, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (source_id, origin_region_id, destination_region_id, predominant_period) DO NOTHING
            """;

    private final DataSourcePort dataSourcePort;
    private final RegionResolver regionResolver;
    private final IngestionLifecycleManager lifecycleManager;
    private final IdGeneratorPort idGeneratorPort;
    private final CsvBatchIngester batchIngester;
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
            return new CsvIngestResult(0, 0, 0);
        }

        try {
            var result = doIngest(key, sourceId);
            lifecycleManager.complete(ingestionRun, result.rowsRead(), result.rowsInserted(), result.rowsRejected());
            return result;
        } catch (RuntimeException e) {
            lifecycleManager.fail(ingestionRun, e);
            throw e;
        }
    }

    private CsvIngestResult doIngest(DatasetObjectKey key, UUID sourceId) {
        var regions = regionResolver.regions();
        var processedKeys = new HashSet<String>();
        return batchIngester.ingest(
                key,
                TravelDistanceMetricCsvRow.class,
                row -> Optional.ofNullable(processRow(row, processedKeys, regions, sourceId)),
                this::batchInsertTravelDistances);
    }

    private void batchInsertTravelDistances(List<TravelDistanceMetricEntity> metrics) {
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int idx) throws java.sql.SQLException {
                TravelDistanceMetricEntity m = metrics.get(idx);
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
                return metrics.size();
            }
        });
    }

    private TravelDistanceMetricEntity processRow(TravelDistanceMetricCsvRow row, Set<String> processedKeys, RegionIndex regions, UUID sourceId) {
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

            var originRegion = regions.byClusterName(row.originCluster());
            var destRegion = regions.byClusterName(row.destCluster());
            if (originRegion.isEmpty() || destRegion.isEmpty()) {
                log.warn("Rejected: region not found for cluster {} / {}", row.originCluster(), row.destCluster());
                return null;
            }

            return TravelDistanceMetricEntity.builder()
                    .id(idGeneratorPort.generate())
                    .sourceId(sourceId)
                    .originRegionId(originRegion.get().id())
                    .destinationRegionId(destRegion.get().id())
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
