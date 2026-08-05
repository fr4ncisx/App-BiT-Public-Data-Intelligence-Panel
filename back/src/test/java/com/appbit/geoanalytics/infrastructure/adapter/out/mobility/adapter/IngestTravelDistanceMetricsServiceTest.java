package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.adapter;

import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
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
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv.TravelDistanceMetricCsvRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.MappingIterator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IngestTravelDistanceMetricsServiceTest {

    @Mock private DatasetObjectStoragePort storagePort;
    @Mock private DataSourcePort dataSourcePort;
    @Mock private GenericCsvReader csvReader;
    @Mock private RegionJpaRepository regionRepository;
    @Mock private IngestionLifecycleManager lifecycleManager;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private JdbcTemplate jdbcTemplate;

    private IngestTravelDistanceMetricsService service;

    private static final DatasetObjectKey TEST_KEY = new DatasetObjectKey("tensor_tempo_deslocamento.csv");
    private static final UUID SOURCE_ID = DomainFixtures.uuidV7();
    private static final UUID REGION_ID = UUID.randomUUID();
    private static final UUID ENTITY_ID = DomainFixtures.uuidV7();

    private IngestionRun mockRun;

    @BeforeEach
    void setUp() {
        when(idGeneratorPort.generate()).thenReturn(ENTITY_ID);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        service = new IngestTravelDistanceMetricsService(
                storagePort, dataSourcePort, csvReader,
                regionRepository, lifecycleManager, transactionTemplate, idGeneratorPort, jdbcTemplate,
                new IngestionProperties(true, 500, true)
        );

        mockRun = IngestionRun.builder()
                .id(new IngestionRunId(DomainFixtures.uuidV7()))
                .sourceId(new DataSourceId(SOURCE_ID))
                .fileName(new SourceFileName("tensor_tempo_deslocamento.csv"))
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
        var entry = new SourceCatalogEntry(SOURCE_ID, "Travel Distance", new SourceFileName("tensor_tempo_deslocamento.csv"),
                DataSourceType.SYNTHETIC_DATASET, "Desc", null, null, null, null);
        when(dataSourcePort.findByFileName(any())).thenReturn(Optional.of(entry));
        when(storagePort.openStream(TEST_KEY)).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        when(lifecycleManager.start("tensor_tempo_deslocamento.csv", SOURCE_ID)).thenReturn(mockRun);
    }

    private TravelDistanceMetricCsvRow createValidRow() {
        return new TravelDistanceMetricCsvRow(
                "CBD_BEIRAMAR", "CENTRO", "false", "100", "1.200", "0.500", "2.000", "MANHA"
        );
    }

    private TravelDistanceMetricCsvRow createRow(String originCluster, String period) {
        return new TravelDistanceMetricCsvRow(
                originCluster, "CENTRO", "false", "100", "1.200", "0.500", "2.000", period
        );
    }

    private RegionEntity createRegion() {
        return RegionEntity.builder().id(REGION_ID).municipality("Florianopolis").build();
    }

    @Test
    void shouldIngestTravelDistanceMetricsSuccessfully() throws Exception {
        mockSourceIdResolution();

        when(csvReader.read(any(InputStream.class), eq(TravelDistanceMetricCsvRow.class)))
                .thenReturn(createStubIterator(List.of(createValidRow())));
        when(regionRepository.findByClusterName("CBD_BEIRAMAR")).thenReturn(Optional.of(createRegion()));
        when(regionRepository.findByClusterName("CENTRO")).thenReturn(Optional.of(createRegion()));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isEqualTo(1);
        assertThat(result.rowsRejected()).isZero();
        verify(lifecycleManager).complete(mockRun, 1, 1, 0);

        var captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

        var setter = captor.getValue();
        assertThat(setter.getBatchSize()).isEqualTo(1);

        var ps = org.mockito.Mockito.mock(PreparedStatement.class);
        setter.setValues(ps, 0);
        verify(ps).setObject(1, ENTITY_ID);
        verify(ps).setObject(2, SOURCE_ID);
        verify(ps).setObject(3, REGION_ID);
        verify(ps).setObject(4, REGION_ID);
        verify(ps).setString(5, "CBD_BEIRAMAR");
        verify(ps).setString(6, "CENTRO");
        verify(ps).setString(7, "MANHA");
        verify(ps).setBoolean(8, false);
        verify(ps).setLong(9, 100L);
        verify(ps).setBigDecimal(10, new BigDecimal("1.200"));
        verify(ps).setBigDecimal(11, new BigDecimal("0.500"));
        verify(ps).setBigDecimal(12, new BigDecimal("2.000"));
    }

    @Test
    void shouldRejectDuplicateRowsInCsv() {
        mockSourceIdResolution();

        var row = createValidRow();
        when(csvReader.read(any(InputStream.class), eq(TravelDistanceMetricCsvRow.class)))
                .thenReturn(createStubIterator(List.of(row, row)));
        when(regionRepository.findByClusterName("CBD_BEIRAMAR")).thenReturn(Optional.of(createRegion()));
        when(regionRepository.findByClusterName("CENTRO")).thenReturn(Optional.of(createRegion()));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(2);
        assertThat(result.rowsInserted()).isEqualTo(1);
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidPeriod() {
        mockSourceIdResolution();

        when(csvReader.read(any(InputStream.class), eq(TravelDistanceMetricCsvRow.class)))
                .thenReturn(createStubIterator(List.of(createRow("CBD_BEIRAMAR", "XUMLA"))));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isZero();
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldRejectRowWhenRegionNotFound() {
        mockSourceIdResolution();

        when(csvReader.read(any(InputStream.class), eq(TravelDistanceMetricCsvRow.class)))
                .thenReturn(createStubIterator(List.of(createValidRow())));
        when(regionRepository.findByClusterName("CBD_BEIRAMAR")).thenReturn(Optional.empty());

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isZero();
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldRejectRowWithMalformedNumbers() {
        mockSourceIdResolution();

        var malformed = new TravelDistanceMetricCsvRow(
                "CBD_BEIRAMAR", "CENTRO", "false", "abc", "1.200", "0.500", "2.000", "MANHA"
        );
        when(csvReader.read(any(InputStream.class), eq(TravelDistanceMetricCsvRow.class)))
                .thenReturn(createStubIterator(List.of(malformed)));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isZero();
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldInsertInBatchesOfFiveHundred() {
        mockSourceIdResolution();

        var rows = new ArrayList<TravelDistanceMetricCsvRow>();
        for (int i = 0; i < 501; i++) {
            rows.add(createRow("CBD_BEIRAMAR-" + i, "MANHA"));
        }
        when(csvReader.read(any(InputStream.class), eq(TravelDistanceMetricCsvRow.class)))
                .thenReturn(createStubIterator(rows));
        when(regionRepository.findByClusterName(anyString())).thenReturn(Optional.of(createRegion()));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(501);
        assertThat(result.rowsInserted()).isEqualTo(501);
        verify(jdbcTemplate, org.mockito.Mockito.times(2)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void shouldReturnEmptyResultWhenRunAlreadyCompleted() {
        mockSourceIdResolution();
        when(lifecycleManager.start("tensor_tempo_deslocamento.csv", SOURCE_ID)).thenReturn(null);

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isZero();
        assertThat(result.rowsInserted()).isZero();
        assertThat(result.rowsRejected()).isZero();
        verifyNoInteractions(storagePort);
    }

    @Test
    void shouldThrowWhenDataSourceNotFound() {
        when(dataSourcePort.findByFileName(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.ingest(TEST_KEY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tensor_tempo_deslocamento.csv");

        verify(lifecycleManager, never()).start(anyString(), any());
    }

    @Test
    void shouldMarkRunAsFailedWhenIngestFails() {
        mockSourceIdResolution();
        when(storagePort.openStream(TEST_KEY)).thenThrow(new RuntimeException("Storage unavailable"));

        assertThatThrownBy(() -> service.ingest(TEST_KEY))
                .isInstanceOf(RuntimeException.class);

        verify(lifecycleManager).fail(eq(mockRun), any(RuntimeException.class));
    }
}
