package com.appbit.geoanalytics.infrastructure.adapter.out.social.adapter;

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
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.csv.SocialIndicatorCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.entity.SocialIndicatorEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.repository.SocialIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestSocialIndicatorsService implements IngestSocialIndicatorsUseCase {

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
    private final SocialIndicatorJpaRepository socialIndicatorRepository;
    private final RegionJpaRepository regionRepository;
    private final IngestionLifecycleManager lifecycleManager;
    private final TransactionTemplate transactionTemplate;
    private final IdGeneratorPort idGeneratorPort;

    @Override
    public IngestSocialIndicatorsResult execute(DatasetObjectKey key) {
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

    private IngestSocialIndicatorsResult ingest(DatasetObjectKey key, UUID sourceId) {
        try (var inputStream = storagePort.openStream(key);
             var iterator = csvReader.read(inputStream, SocialIndicatorCsvRow.class)) {

            var inserted = 0;
            var rejected = 0;
            var readCount = 0;
            var processedKeys = new HashSet<String>();

            while (iterator.hasNext()) {
                readCount++;
                var entity = processRow(iterator.next(), processedKeys, sourceId);

                if (entity != null) {
                    socialIndicatorRepository.insertIgnoreConflict(
                            entity.getId(), entity.getRegionId(), entity.getSourceId(),
                            entity.getIndicatorType(), entity.getScore(), entity.getUnit(),
                            entity.getGapLevel(), entity.getConfidenceLevel(),
                            entity.getDescription(), entity.getCreatedAt()
                    );
                    inserted++;
                } else {
                    rejected++;
                }
            }

            return IngestSocialIndicatorsResult.of(readCount, inserted, rejected);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest CSV stream: " + e.getMessage(), e);
        }
    }

    private SocialIndicatorEntity processRow(SocialIndicatorCsvRow row, Set<String> processedKeys, UUID sourceId) {
        var dedupeKey = row.regionCode().trim() + "|" + row.indicatorType().trim();
        if (!processedKeys.add(dedupeKey)) return null;

        try {
            var regionId = resolveRegionId(row.regionCode().trim());
            var score = parseScore(row.score());
            validateRow(row.indicatorType().trim(), score, row.unit().trim(), row.gapLevel().trim(), row.confidenceLevel().trim(), row.description());

            return createEntity(row, regionId, sourceId, score);
        } catch (RuntimeException _) {
            return null;
        }
    }

    private void validateRow(String indicatorType, BigDecimal score, String unit, String gapLevel, String confidenceLevel, String description) {
        if (!VALID_INDICATOR_TYPES.contains(indicatorType)) throw new IllegalArgumentException("Invalid indicator type");
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.ONE) > 0) throw new IllegalArgumentException("Score out of bounds");
        if (score.scale() > 4) throw new IllegalArgumentException("Score precision too high");
        if (!VALID_UNITS.contains(unit)) throw new IllegalArgumentException("Invalid unit");
        if (!VALID_GAP_LEVELS.contains(gapLevel)) throw new IllegalArgumentException("Invalid gap level");
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

    private UUID resolveSourceId(String fileName) {
        return dataSourcePort.findByFileName(new SourceFileName(fileName))
                .orElseThrow(() -> new IllegalArgumentException("Data source not found: " + fileName))
                .id();
    }

    private UUID resolveRegionId(String regionCode) {
        return regionRepository.findByRegionCode(regionCode)
                .map(RegionEntity::getId)
                .orElseThrow(() -> new IllegalArgumentException("Region not found for code: " + regionCode));
    }

    private BigDecimal parseScore(String scoreStr) {
        try {
            return new BigDecimal(scoreStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid score value");
        }
    }
}
