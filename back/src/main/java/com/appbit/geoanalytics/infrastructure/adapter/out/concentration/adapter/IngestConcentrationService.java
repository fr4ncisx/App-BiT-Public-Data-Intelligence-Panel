package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.adapter;

import com.appbit.geoanalytics.application.concentration.in.IngestConcentrationResult;
import com.appbit.geoanalytics.application.concentration.in.IngestConcentrationUseCase;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.AntennaJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.csv.ConcentrationCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.entity.ConcentrationMetricEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.repository.ConcentrationMetricJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestConcentrationService implements IngestConcentrationUseCase {

    private static final Set<String> VALID_PERIODS = Set.of("MADRUGADA", "MANHA", "TARDE", "NOITE");
    private static final BigDecimal MIN_LAT = new BigDecimal("-90");
    private static final BigDecimal MAX_LAT = new BigDecimal("90");
    private static final BigDecimal MIN_LON = new BigDecimal("-180");
    private static final BigDecimal MAX_LON = new BigDecimal("180");

    private final DatasetObjectStoragePort storagePort;
    private final DataSourcePort dataSourcePort;
    private final GenericCsvReader csvReader;
    private final ConcentrationMetricJpaRepository concentrationRepository;
    private final AntennaJpaRepository antennaRepository;
    private final RegionJpaRepository regionRepository;
    private final IngestionLifecycleManager lifecycleManager;
    private final TransactionTemplate transactionTemplate;
    private final IdGeneratorPort idGeneratorPort;

    @Override
    public IngestConcentrationResult execute(DatasetObjectKey key) {
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

    private IngestConcentrationResult ingest(DatasetObjectKey key, UUID sourceId) {
        try (var inputStream = storagePort.openStream(key);
             var iterator = csvReader.read(inputStream, ConcentrationCsvRow.class)) {

            var existingEcgis = new HashSet<>(antennaRepository.findAllEcgis());
            var processedKeys = new HashSet<String>();
            var metricsToInsert = new ArrayList<ConcentrationMetricEntity>();
            var rejected = 0;
            var readCount = 0;

            while (iterator.hasNext()) {
                readCount++;
                var entity = processRow(iterator.next(), existingEcgis, processedKeys, sourceId);

                if (entity != null) {
                    metricsToInsert.add(entity);
                } else {
                    rejected++;
                }
            }

            metricsToInsert.forEach(metric ->
                    concentrationRepository.insertIgnoreConflict(
                            metric.getId(), metric.getSourceId(), metric.getRegionId(),
                            metric.getEcgi(), metric.getClusterName(), metric.getMunicipality(),
                            metric.getDayDate(), metric.getPeriod(), metric.getActiveUsers(),
                            metric.getSessions(), metric.getDownloadBytes(), metric.getUploadBytes(),
                            metric.getAverageSessionDurationSeconds(), metric.getAverageDropRate(),
                            metric.getAverageCongestion(), metric.getTotalCalls(), metric.getTotalMessages(),
                            metric.getLatitude(), metric.getLongitude(), metric.getCreatedAt()
                    )
            );

            return IngestConcentrationResult.of(readCount, metricsToInsert.size(), rejected);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest CSV stream: " + e.getMessage(), e);
        }
    }

    private ConcentrationMetricEntity processRow(ConcentrationCsvRow row, Set<String> existingEcgis, Set<String> processedKeys, UUID sourceId) {
        if (!isValidRow(row, existingEcgis)) return null;

        var compositeKey = row.ecgi().trim() + "|" + row.dayDate().trim() + "|" + row.periodo().trim().toUpperCase();
        if (!processedKeys.add(compositeKey)) return null;

        var region = regionRepository.findByClusterNameAndMunicipality(row.cluster().trim(), row.municipio().trim());
        if (region.isEmpty()) return null;

        return createMetric(row, sourceId, region.get().getId());
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

    private UUID resolveSourceId(String fileName) {
        return dataSourcePort.findByFileName(new SourceFileName(fileName))
                .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + fileName))
                .id();
    }
}
