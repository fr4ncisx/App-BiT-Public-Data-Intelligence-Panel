package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.adapter;

import com.appbit.geoanalytics.application.concentration.in.IngestConcentrationResult;
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
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.AntennaJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.csv.ConcentrationCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IngestConcentrationServiceTest {

    @Mock private DatasetObjectStoragePort storagePort;
    @Mock private DataSourcePort dataSourcePort;
    @Mock private GenericCsvReader csvReader;
    @Mock private AntennaJpaRepository antennaRepository;
    @Mock private RegionJpaRepository regionRepository;
    @Mock private IngestionLifecycleManager lifecycleManager;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private JdbcTemplate jdbcTemplate;

    private IngestConcentrationService service;

    private static final DatasetObjectKey TEST_KEY = new DatasetObjectKey("tensor_concentracao.csv");
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
        service = new IngestConcentrationService(
                storagePort, dataSourcePort, csvReader,
                antennaRepository, regionRepository,
                lifecycleManager, transactionTemplate, idGeneratorPort, jdbcTemplate,
                new IngestionProperties(true, 500, true)
        );

        mockRun = IngestionRun.builder()
                .id(new IngestionRunId(DomainFixtures.uuidV7()))
                .sourceId(new DataSourceId(SOURCE_ID))
                .fileName(new SourceFileName("tensor_concentracao.csv"))
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
        var entry = new SourceCatalogEntry(SOURCE_ID, "Concentration", new SourceFileName("tensor_concentracao.csv"), DataSourceType.SYNTHETIC_DATASET, "Desc", null, null, null, null);
        when(dataSourcePort.findByFileName(any())).thenReturn(Optional.of(entry));
        when(storagePort.openStream(TEST_KEY)).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        when(lifecycleManager.start("tensor_concentracao.csv", SOURCE_ID)).thenReturn(mockRun);
    }

    private ConcentrationCsvRow createValidRow() {
        return new ConcentrationCsvRow(
                "1234567890123", "CBD_BEIRAMAR", "Florianopolis",
                "2026-03-01", "MANHA", "100", "50", "1000000", "500000", "120",
                "0.0100", "0.050", "10", "5", "-27.595400", "-48.548000"
        );
    }

    @Test
    void shouldIngestConcentrationSuccessfully() {
        mockSourceIdResolution();

        var row = createValidRow();
        var region = RegionEntity.builder().id(REGION_ID).build();

        when(csvReader.read(any(InputStream.class), eq(ConcentrationCsvRow.class))).thenReturn(createStubIterator(List.of(row)));
        when(antennaRepository.findAllEcgis()).thenReturn(Set.of("1234567890123"));
        when(regionRepository.findByClusterName("CBD_BEIRAMAR")).thenReturn(Optional.of(region));

        IngestConcentrationResult result = service.execute(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isEqualTo(1);
        verify(lifecycleManager).complete(mockRun, 1, 1, 0);
    }

    @Test
    void shouldRejectDuplicateKeyInCsv() {
        mockSourceIdResolution();

        var row1 = createValidRow();
        var row2 = createValidRow();
        var region = RegionEntity.builder().id(REGION_ID).build();

        when(csvReader.read(any(InputStream.class), eq(ConcentrationCsvRow.class))).thenReturn(createStubIterator(List.of(row1, row2)));
        when(antennaRepository.findAllEcgis()).thenReturn(Set.of("1234567890123"));
        when(regionRepository.findByClusterName("CBD_BEIRAMAR")).thenReturn(Optional.of(region));

        IngestConcentrationResult result = service.execute(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(2);
        assertThat(result.rowsInserted()).isEqualTo(1);
    }

    @Test
    void shouldRejectRowWithMalformedNumericFieldWithoutFailingRun() {
        mockSourceIdResolution();

        var row = new ConcentrationCsvRow(
                "1234567890123", "CBD_BEIRAMAR", "Florianopolis",
                "2026-03-01", "MANHA", "abc", "50", "1000000", "500000", "120",
                "0.0100", "0.050", "10", "5", "-27.595400", "-48.548000"
        );
        var region = RegionEntity.builder().id(REGION_ID).build();

        when(csvReader.read(any(InputStream.class), eq(ConcentrationCsvRow.class))).thenReturn(createStubIterator(List.of(row)));
        when(antennaRepository.findAllEcgis()).thenReturn(Set.of("1234567890123"));
        when(regionRepository.findByClusterName("CBD_BEIRAMAR")).thenReturn(Optional.of(region));

        IngestConcentrationResult result = service.execute(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isZero();
        verify(lifecycleManager).complete(mockRun, 1, 0, 1);
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