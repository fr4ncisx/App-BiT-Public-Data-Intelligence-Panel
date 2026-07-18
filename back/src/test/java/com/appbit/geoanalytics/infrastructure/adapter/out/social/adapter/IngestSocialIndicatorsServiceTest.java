package com.appbit.geoanalytics.infrastructure.adapter.out.social.adapter;

import com.appbit.geoanalytics.application.shared.port.out.IdGeneratorPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.domain.ingestion.vo.IngestionRunId;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.domain.testing.DomainFixtures;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.csv.SocialIndicatorCsvRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.MappingIterator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IngestSocialIndicatorsServiceTest {

    @Mock private DatasetObjectStoragePort storagePort;
    @Mock private DataSourcePort dataSourcePort;
    @Mock private GenericCsvReader csvReader;
    @Mock private RegionJpaRepository regionRepository;
    @Mock private IngestionLifecycleManager lifecycleManager;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private JdbcTemplate jdbcTemplate;

    private IngestSocialIndicatorsService service;

    private static final DatasetObjectKey TEST_KEY = new DatasetObjectKey("social_indicators_seed.csv");
    private static final UUID SOURCE_ID = DomainFixtures.uuidV7();
    private static final UUID REGION_ID = UUID.randomUUID();
    private IngestionRun mockRun;

    @BeforeEach
    void setUp() {
        when(idGeneratorPort.generate()).thenReturn(DomainFixtures.uuidV7());
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        service = new IngestSocialIndicatorsService(
                storagePort, dataSourcePort, csvReader, regionRepository,
                lifecycleManager, transactionTemplate, idGeneratorPort, jdbcTemplate
        );

        mockRun = IngestionRun.builder()
                .id(new IngestionRunId(DomainFixtures.uuidV7()))
                .sourceId(new DataSourceId(SOURCE_ID))
                .fileName(new SourceFileName(TEST_KEY.value()))
                .state(IngestionState.RUNNING)
                .startedAt(Instant.now())
                .build();
    }

    private <T> MappingIterator<T> createStubIterator(List<T> items) {
        var backingIterator = items.iterator();
        return new MappingIterator<>(null, null, null, null, false, null) {
            @Override
            public boolean hasNext() {
                return backingIterator.hasNext();
            }
            @Override
            public T next() {
                return backingIterator.next();
            }
            @Override
            public void close() {}
        };
    }

    private void mockSourceIdResolution() {
        var entry = new SourceCatalogEntry(SOURCE_ID, "Social Indicators", new SourceFileName(TEST_KEY.value()), DataSourceType.SEED_DATA, "Desc", null, null, null, null);
        when(dataSourcePort.findByFileName(any())).thenReturn(Optional.of(entry));
        when(storagePort.openStream(TEST_KEY)).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        when(lifecycleManager.start(TEST_KEY.value(), SOURCE_ID)).thenReturn(mockRun);
    }

    @Test
    void shouldIngestSocialIndicatorsSuccessfully() {
        mockSourceIdResolution();

        var region = RegionEntity.builder().id(REGION_ID).build();
        when(regionRepository.findByRegionCode("REG_TRINDADE")).thenReturn(Optional.of(region));

        var row = new SocialIndicatorCsvRow("TRINDADE", "TRAINING", "0.7200", "PROGRAMS", "MEDIUM", "HIGH", "Programas de formacion");
        when(csvReader.read(any(InputStream.class), eq(SocialIndicatorCsvRow.class))).thenReturn(createStubIterator(List.of(row)));

        var result = service.execute(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isEqualTo(1);

        verify(jdbcTemplate).batchUpdate(any(String.class), any(org.springframework.jdbc.core.BatchPreparedStatementSetter.class));
        verify(lifecycleManager).complete(mockRun, 1, 1, 0);
    }

    @Test
    void shouldCreateMissingRegionOnTheFly() {
        mockSourceIdResolution();
        var newRegionId = UUID.randomUUID();
        var createdRegion = RegionEntity.builder().id(newRegionId).build();

        when(regionRepository.findByRegionCode("REG_NOVO_CODIGO"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(createdRegion));

        var row = new SocialIndicatorCsvRow("NOVO_CODIGO", "TRAINING", "0.7200", "PROGRAMS", "MEDIUM", "HIGH", "Programas de formacion");
        when(csvReader.read(any(InputStream.class), eq(SocialIndicatorCsvRow.class))).thenReturn(createStubIterator(List.of(row)));

        var result = service.execute(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isEqualTo(1);
        verify(regionRepository).insertIgnoreConflict(any(), eq("REG_NOVO_CODIGO"), eq("NOVO_CODIGO"), eq("NOVO_CODIGO"), eq("Unknown"), eq(new BigDecimal("-27.600000")), eq(new BigDecimal("-48.600000")), any());
    }

    @Test
    void shouldMarkRunAsFailedWhenIngestThrows() {
        mockSourceIdResolution();
        when(storagePort.openStream(TEST_KEY)).thenThrow(new RuntimeException("Storage unavailable"));

        assertThatThrownBy(() -> service.execute(TEST_KEY))
                .isInstanceOf(RuntimeException.class);

        verify(lifecycleManager).fail(eq(mockRun), any(RuntimeException.class));
    }
}