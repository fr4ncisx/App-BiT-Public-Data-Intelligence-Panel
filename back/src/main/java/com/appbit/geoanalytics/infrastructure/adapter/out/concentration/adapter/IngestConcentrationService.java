package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.adapter;

import com.appbit.geoanalytics.application.concentration.in.IngestConcentrationResult;
import com.appbit.geoanalytics.application.concentration.in.IngestConcentrationUseCase;
import com.appbit.geoanalytics.application.ingestion.in.CsvIngestService;
import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.AntennaJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.csv.ConcentrationCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.entity.ConcentrationMetricEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.CsvBatchIngester;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.RegionIndex;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.RegionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class IngestConcentrationService implements IngestConcentrationUseCase, CsvIngestService {

    private static final Set<String> VALID_PERIODS = Set.of("MADRUGADA", "MANHA", "TARDE", "NOITE");
    private static final BigDecimal MIN_LAT = new BigDecimal("-90");
    private static final BigDecimal MAX_LAT = new BigDecimal("90");
    private static final BigDecimal MIN_LON = new BigDecimal("-180");
    private static final BigDecimal MAX_LON = new BigDecimal("180");

    private static final String INSERT_SQL = """
            INSERT INTO concentration_metrics (
                id, source_id, region_id, ecgi, cluster_name, municipality,
                day_date, period, active_users, sessions, download_bytes, upload_bytes,
                average_session_duration_seconds, average_drop_rate, average_congestion,
                total_calls, total_messages, latitude, longitude, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (source_id, ecgi, day_date, period) DO NOTHING
            """;

    private final DataSourcePort dataSourcePort;
    private final AntennaJpaRepository antennaRepository;
    private final RegionResolver regionResolver;
    private final IngestionLifecycleManager lifecycleManager;
    private final IdGeneratorPort idGeneratorPort;
    private final CsvBatchIngester batchIngester;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public IngestConcentrationResult execute(DatasetObjectKey key) {
        var sourceId = resolveSourceId(key.value());
        var ingestionRun = lifecycleManager.start(key.value(), sourceId);

        if (ingestionRun == null) {
            return IngestConcentrationResult.of(0, 0, 0);
        }

        try {
            var result = ingest(key, sourceId);
            lifecycleManager.complete(ingestionRun, result.rowsRead(), result.rowsInserted(), result.rowsRejected());
            return result;
        } catch (RuntimeException e) {
            lifecycleManager.fail(ingestionRun, e);
            throw e;
        }
    }

    private IngestConcentrationResult ingest(DatasetObjectKey key, UUID sourceId) {
        var existingEcgis = new HashSet<>(antennaRepository.findAllEcgis());
        var regions = regionResolver.regions();
        var processedKeys = new HashSet<String>();

        var result = batchIngester.ingest(
                key,
                ConcentrationCsvRow.class,
                row -> Optional.ofNullable(processRow(row, existingEcgis, processedKeys, regions, sourceId)),
                this::batchInsertConcentrationMetrics);

        return IngestConcentrationResult.of(result.rowsRead(), result.rowsInserted(), result.rowsRejected());
    }

    private void batchInsertConcentrationMetrics(List<ConcentrationMetricEntity> metrics) {
        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int idx) throws java.sql.SQLException {
                ConcentrationMetricEntity metric = metrics.get(idx);
                ps.setObject(1, metric.getId());
                ps.setObject(2, metric.getSourceId());
                ps.setObject(3, metric.getRegionId());
                ps.setString(4, metric.getEcgi());
                ps.setString(5, metric.getClusterName());
                ps.setString(6, metric.getMunicipality());
                ps.setObject(7, metric.getDayDate());
                ps.setString(8, metric.getPeriod());
                ps.setLong(9, metric.getActiveUsers());
                ps.setLong(10, metric.getSessions());
                ps.setLong(11, metric.getDownloadBytes());
                ps.setLong(12, metric.getUploadBytes());
                ps.setInt(13, metric.getAverageSessionDurationSeconds());
                ps.setBigDecimal(14, metric.getAverageDropRate());
                ps.setBigDecimal(15, metric.getAverageCongestion());
                ps.setInt(16, metric.getTotalCalls());
                ps.setInt(17, metric.getTotalMessages());
                ps.setBigDecimal(18, metric.getLatitude());
                ps.setBigDecimal(19, metric.getLongitude());
                ps.setTimestamp(20, Timestamp.from(metric.getCreatedAt()));
            }

            @Override
            public int getBatchSize() {
                return metrics.size();
            }
        });
    }

    private ConcentrationMetricEntity processRow(ConcentrationCsvRow row, Set<String> existingEcgis, Set<String> processedKeys, RegionIndex regions, UUID sourceId) {
        if (!isValidRow(row, existingEcgis)) {
            log.warn("Rejected ECGI {}: validation failed", row.ecgi().trim());
            return null;
        }

        var compositeKey = row.ecgi().trim() + "|" + row.dayDate().trim() + "|" + row.periodo().trim().toUpperCase();
        if (!processedKeys.add(compositeKey)) {
            log.warn("Rejected duplicate: {}", compositeKey);
            return null;
        }

        var region = regions.byClusterName(row.cluster());
        if (region.isEmpty()) {
            log.warn("Rejected ECGI {}: region not found for cluster={} municipio={}", row.ecgi().trim(), row.cluster().trim(), row.municipio().trim());
            return null;
        }

        try {
            return createMetric(row, sourceId, region.get().id());
        } catch (RuntimeException e) {
            log.warn("Rejected ECGI {}: malformed metric data: {}", row.ecgi().trim(), e.getMessage());
            return null;
        }
    }

    private boolean isValidRow(ConcentrationCsvRow row, Set<String> existingEcgis) {
        var ecgi = row.ecgi().trim();
        if (!existingEcgis.contains(ecgi)) return false;

        try {
            LocalDate.parse(row.dayDate().trim());

            var lat = new BigDecimal(row.lat().trim());
            var lon = new BigDecimal(row.lon().trim());

            if (lat.compareTo(MIN_LAT) < 0 || lat.compareTo(MAX_LAT) > 0) return false;
            if (lon.compareTo(MIN_LON) < 0 || lon.compareTo(MAX_LON) > 0) return false;
            if (lat.compareTo(BigDecimal.ZERO) == 0 && lon.compareTo(BigDecimal.ZERO) == 0) return false;
        } catch (RuntimeException _) {
            return false;
        }

        var periodo = row.periodo().trim().toUpperCase();
        if (!VALID_PERIODS.contains(periodo)) return false;

        return !row.cluster().isBlank() && !row.municipio().isBlank();
    }

    private ConcentrationMetricEntity createMetric(ConcentrationCsvRow row, UUID sourceId, UUID regionId) {
        return ConcentrationMetricEntity.builder()
                .id(idGeneratorPort.generate())
                .sourceId(sourceId)
                .regionId(regionId)
                .ecgi(row.ecgi().trim())
                .clusterName(row.cluster().trim())
                .municipality(row.municipio().trim())
                .dayDate(LocalDate.parse(row.dayDate().trim()))
                .period(row.periodo().trim().toUpperCase())
                .activeUsers(Long.parseLong(row.nUsuarios().trim()))
                .sessions(Long.parseLong(row.nSessoes().trim()))
                .downloadBytes(Long.parseLong(row.downloadBytes().trim()))
                .uploadBytes(Long.parseLong(row.uploadBytes().trim()))
                .averageSessionDurationSeconds(Integer.parseInt(row.durMediaS().trim()))
                .averageDropRate(new BigDecimal(row.dropPctMedio().trim()).setScale(6, RoundingMode.HALF_UP))
                .averageCongestion(new BigDecimal(row.congestionamentoMedio().trim()).setScale(6, RoundingMode.HALF_UP))
                .totalCalls(Integer.parseInt(row.chamadasTotal().trim()))
                .totalMessages(Integer.parseInt(row.mensagensTotal().trim()))
                .latitude(new BigDecimal(row.lat().trim()))
                .longitude(new BigDecimal(row.lon().trim()))
                .createdAt(Instant.now())
                .build();
    }

    @Override
    public String supportedFileName() {
        return "tensor_concentracao.csv";
    }

    @Override
    public CsvIngestResult ingest(DatasetObjectKey key) {
        var result = execute(key);
        return new CsvIngestResult(result.rowsRead(), result.rowsInserted(), result.rowsRejected());
    }

    private UUID resolveSourceId(String fileName) {
        return dataSourcePort.findByFileName(new SourceFileName(fileName))
                .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + fileName))
                .id();
    }
}