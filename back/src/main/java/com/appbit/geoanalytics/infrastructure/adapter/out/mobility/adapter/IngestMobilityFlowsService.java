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
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv.MobilityFlowCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.entity.MobilityFlowEntity;
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
public class IngestMobilityFlowsService implements CsvIngestService {

    private static final Set<String> VALID_PERIODS = Set.of("MADRUGADA", "MANHA", "TARDE", "NOITE");

    private static final String INSERT_SQL = """
            INSERT INTO mobility_flows (
                id, source_id, origin_region_id, destination_region_id, origin_ecgi, destination_ecgi,
                origin_latitude, origin_longitude, destination_latitude, destination_longitude,
                origin_cluster_name, destination_cluster_name, origin_municipality, destination_municipality,
                users_count, transitions_count, distance_km, predominant_period, origin_cluster_percentage, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (source_id, origin_ecgi, destination_ecgi, predominant_period) DO NOTHING
            """;

    private final DataSourcePort dataSourcePort;
    private final RegionResolver regionResolver;
    private final IngestionLifecycleManager lifecycleManager;
    private final IdGeneratorPort idGeneratorPort;
    private final CsvBatchIngester batchIngester;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public String supportedFileName() {
        return "tensor_fluxo_vias.csv";
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
                MobilityFlowCsvRow.class,
                row -> Optional.ofNullable(processRow(row, processedKeys, regions, sourceId)),
                this::batchInsertMobilityFlows);
    }

    private void batchInsertMobilityFlows(List<MobilityFlowEntity> flows) {
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int idx) throws java.sql.SQLException {
                MobilityFlowEntity f = flows.get(idx);
                ps.setObject(1, f.getId());
                ps.setObject(2, f.getSourceId());
                ps.setObject(3, f.getOriginRegionId());
                ps.setObject(4, f.getDestinationRegionId());
                ps.setString(5, f.getOriginEcgi());
                ps.setString(6, f.getDestinationEcgi());
                ps.setBigDecimal(7, f.getOriginLatitude());
                ps.setBigDecimal(8, f.getOriginLongitude());
                ps.setBigDecimal(9, f.getDestinationLatitude());
                ps.setBigDecimal(10, f.getDestinationLongitude());
                ps.setString(11, f.getOriginClusterName());
                ps.setString(12, f.getDestinationClusterName());
                ps.setString(13, f.getOriginMunicipality());
                ps.setString(14, f.getDestinationMunicipality());
                ps.setLong(15, f.getUsersCount());
                ps.setLong(16, f.getTransitionsCount());
                ps.setBigDecimal(17, f.getDistanceKm());
                ps.setString(18, f.getPredominantPeriod());
                ps.setBigDecimal(19, f.getOriginClusterPercentage());
                ps.setTimestamp(20, Timestamp.from(f.getCreatedAt()));
            }

            @Override
            public int getBatchSize() {
                return flows.size();
            }
        });
    }

    private MobilityFlowEntity processRow(MobilityFlowCsvRow row, Set<String> processedKeys, RegionIndex regions, UUID sourceId) {
        var dedupKey = row.originEcgi().trim() + "|" + row.destEcgi().trim() + "|" + row.periodo().trim().toUpperCase();
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

            return MobilityFlowEntity.builder()
                    .id(idGeneratorPort.generate())
                    .sourceId(sourceId)
                    .originRegionId(originRegion.get().id())
                    .destinationRegionId(destRegion.get().id())
                    .originEcgi(row.originEcgi().trim())
                    .destinationEcgi(row.destEcgi().trim())
                    .originLatitude(new BigDecimal(row.originLat().trim()))
                    .originLongitude(new BigDecimal(row.originLon().trim()))
                    .destinationLatitude(new BigDecimal(row.destLat().trim()))
                    .destinationLongitude(new BigDecimal(row.destLon().trim()))
                    .originClusterName(row.originCluster().trim())
                    .destinationClusterName(row.destCluster().trim())
                    .originMunicipality(originRegion.get().municipality())
                    .destinationMunicipality(destRegion.get().municipality())
                    .usersCount(Long.parseLong(row.nUsuarios().trim()))
                    .transitionsCount(Long.parseLong(row.nTransicoes().trim()))
                    .distanceKm(new BigDecimal(row.distanciaKm().trim()).setScale(3, RoundingMode.HALF_UP))
                    .predominantPeriod(periodo)
                    .originClusterPercentage(new BigDecimal(row.pctOrigemCluster().trim()).setScale(3, RoundingMode.HALF_UP))
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
