package com.appbit.geoanalytics.infrastructure.adapter.out.social.adapter;

import com.appbit.geoanalytics.application.ingestion.in.CsvIngestService;
import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.application.social.in.IngestSocialIndicatorsResult;
import com.appbit.geoanalytics.application.social.in.IngestSocialIndicatorsUseCase;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.csv.SocialIndicatorCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.entity.SocialIndicatorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
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
public class IngestSocialIndicatorsService implements IngestSocialIndicatorsUseCase, CsvIngestService {

    private static final Set<String> VALID_INDICATOR_TYPES = Set.of(
            "TRAINING", "EMPLOYABILITY", "SOCIAL_EXPERIENCE", "MENTORSHIP", "MENTAL_HEALTH"
    );

    private static final Set<String> VALID_UNITS = Set.of(
            "INDEX", "PERCENTAGE", "SCORE", "ACTIVE_USERS", "PROGRAMS", "SERVICES", "BYTES", "SECONDS"
    );

    private static final Set<String> VALID_GAP_LEVELS = Set.of(
            "LOW", "MEDIUM", "HIGH", "CRITICAL", "UNKNOWN"
    );

    private static final Set<String> VALID_CONFIDENCE_LEVELS = Set.of(
            "LOW", "MEDIUM", "HIGH"
    );

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
    public IngestSocialIndicatorsResult execute(DatasetObjectKey key) {
        var sourceId = resolveSourceId(key.value());
        var ingestionRun = lifecycleManager.start(key.value(), sourceId);

        if (ingestionRun == null) {
            return IngestSocialIndicatorsResult.of(0, 0, 0);
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

    private IngestSocialIndicatorsResult ingest(DatasetObjectKey key, UUID sourceId) {
        try (var inputStream = storagePort.openStream(key);
             var iterator = csvReader.read(inputStream, SocialIndicatorCsvRow.class)) {

            var rejected = 0;
            var readCount = 0;
            var processedKeys = new HashSet<String>();
            var indicatorsToInsert = new ArrayList<SocialIndicatorEntity>();

            while (iterator.hasNext()) {
                readCount++;
                var entity = processRow(iterator.next(), processedKeys, sourceId);

                if (entity != null) {
                    indicatorsToInsert.add(entity);
                } else {
                    rejected++;
                }
            }

            batchInsertSocialIndicators(indicatorsToInsert);

            return IngestSocialIndicatorsResult.of(readCount, indicatorsToInsert.size(), rejected);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest CSV stream: " + e.getMessage(), e);
        }
    }

    private void batchInsertSocialIndicators(List<SocialIndicatorEntity> indicators) {
        String sql = """
            INSERT INTO social_indicators (
                id, region_id, source_id, indicator_type, score, unit, gap_level, confidence_level, description, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (source_id, region_id, indicator_type) DO NOTHING
            """;

        int batchSize = ingestionProperties.batchSize();
        for (int i = 0; i < indicators.size(); i += batchSize) {
            List<SocialIndicatorEntity> batch = indicators.subList(i, Math.min(i + batchSize, indicators.size()));
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int idx) throws java.sql.SQLException {
                    SocialIndicatorEntity ind = batch.get(idx);
                    ps.setObject(1, ind.getId());
                    ps.setObject(2, ind.getRegionId());
                    ps.setObject(3, ind.getSourceId());
                    ps.setString(4, ind.getIndicatorType());
                    ps.setBigDecimal(5, ind.getScore());
                    ps.setString(6, ind.getUnit());
                    ps.setString(7, ind.getGapLevel());
                    ps.setString(8, ind.getConfidenceLevel());
                    ps.setString(9, ind.getDescription());
                    ps.setTimestamp(10, Timestamp.from(ind.getCreatedAt()));
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private SocialIndicatorEntity processRow(SocialIndicatorCsvRow row, Set<String> processedKeys, UUID sourceId) {
        var dedupeKey = row.regionCode().trim() + "|" + row.indicatorType().trim();
        if (!processedKeys.add(dedupeKey)) {
            log.warn("Rejected duplicate: {}", dedupeKey);
            return null;
        }

        try {
            var regionId = resolveRegionId(row.regionCode().trim());
            var score = parseScore(row.score());
            validateRow(row.indicatorType().trim(), score, row.unit().trim(), row.gapLevel().trim(), row.confidenceLevel().trim(), row.description());

            return createEntity(row, regionId, sourceId, score);
        } catch (IllegalArgumentException e) {
            log.warn("Rejected {}/{}: {}", row.regionCode(), row.indicatorType(), e.getMessage());
            return null;
        }
    }

    private void validateRow(String indicatorType, BigDecimal score, String unit, String gapLevel, String confidenceLevel, String description) {
        if (!VALID_INDICATOR_TYPES.contains(indicatorType)) throw new IllegalArgumentException("Invalid indicator type");
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.ONE) > 0) throw new IllegalArgumentException("Score out of bounds");
        if (score.scale() > 4) throw new IllegalArgumentException("Score precision too high");
        if (!VALID_UNITS.contains(unit)) throw new IllegalArgumentException("Invalid unit");
        if (!VALID_GAP_LEVELS.contains(gapLevel)) throw new IllegalArgumentException("Invalid gap level");
        if (!VALID_CONFIDENCE_LEVELS.contains(confidenceLevel)) throw new IllegalArgumentException("Invalid confidence level");
        if (description.isBlank() || description.trim().length() < 5 || description.trim().length() > 500) throw new IllegalArgumentException("Invalid description length");
    }

    private SocialIndicatorEntity createEntity(SocialIndicatorCsvRow row, UUID regionId, UUID sourceId, BigDecimal score) {
        return SocialIndicatorEntity.builder()
                .id(idGeneratorPort.generate())
                .regionId(regionId)
                .sourceId(sourceId)
                .indicatorType(row.indicatorType().trim())
                .score(score)
                .unit(row.unit().trim())
                .gapLevel(row.gapLevel().trim())
                .confidenceLevel(row.confidenceLevel().trim())
                .description(row.description().trim())
                .createdAt(Instant.now())
                .build();
    }

    @Override
    public String supportedFileName() {
        return "social_indicators_seed.csv";
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

    private UUID resolveRegionId(String regionCode) {
        var fullCode = "REG_" + regionCode;
        var existing = regionRepository.findByRegionCode(fullCode);
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        var id = idGeneratorPort.generate();
        var placeholderLat = new BigDecimal("-27.600000");
        var placeholderLon = new BigDecimal("-48.600000");
        regionRepository.insertIgnoreConflict(
                id, fullCode, regionCode, regionCode, "Unknown",
                placeholderLat, placeholderLon, Instant.now()
        );
        return regionRepository.findByRegionCode(fullCode)
                .map(RegionEntity::getId)
                .orElseThrow(() -> new IllegalArgumentException("Failed to create region: " + regionCode));
    }

    private BigDecimal parseScore(String scoreStr) {
        try {
            return new BigDecimal(scoreStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid score value");
        }
    }
}
