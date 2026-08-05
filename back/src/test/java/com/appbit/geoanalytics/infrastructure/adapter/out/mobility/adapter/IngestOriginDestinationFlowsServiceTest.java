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
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.CsvBatchIngester;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.RegionIndex;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline.RegionResolver;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv.OriginDestinationFlowCsvRow;
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
import org.springframework.transaction.TransactionStatus;
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
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IngestOriginDestinationFlowsServiceTest {

    @Mock private DatasetObjectStoragePort storagePort;
    @Mock private DataSourcePort dataSourcePort;
    @Mock private GenericCsvReader csvReader;
    @Mock private RegionResolver regionResolver;
    @Mock private IngestionLifecycleManager lifecycleManager;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private IdGeneratorPort idGeneratorPort;
    @Mock private JdbcTemplate jdbcTemplate;

    private IngestOriginDestinationFlowsService service;

    private static final DatasetObjectKey TEST_KEY = new DatasetObjectKey("tensor_od.csv");
    private static final UUID SOURCE_ID = DomainFixtures.uuidV7();
    private static final UUID REGION_ID = UUID.randomUUID();
    private static final UUID ENTITY_ID = DomainFixtures.uuidV7();

    private IngestionRun mockRun;

    @BeforeEach
    void setUp() {
        when(idGeneratorPort.generate()).thenReturn(ENTITY_ID);
        doAnswer(invocation -> {
            Consumer<TransactionStatus> action = invocation.getArgument(0);
            action.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        var batchIngester = new CsvBatchIngester(
                storagePort, csvReader, transactionTemplate, new IngestionProperties(true, 500, true));
        service = new IngestOriginDestinationFlowsService(
                dataSourcePort, regionResolver, lifecycleManager, idGeneratorPort, batchIngester, jdbcTemplate
        );

        mockRun = IngestionRun.builder()
                .id(new IngestionRunId(DomainFixtures.uuidV7()))
                .sourceId(new DataSourceId(SOURCE_ID))
                .fileName(new SourceFileName("tensor_od.csv"))
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
        var entry = new SourceCatalogEntry(SOURCE_ID, "OD Flows", new SourceFileName("tensor_od.csv"),
                DataSourceType.SYNTHETIC_DATASET, "Desc", null, null, null, null);
        when(dataSourcePort.findByFileName(any())).thenReturn(Optional.of(entry));
        when(storagePort.openStream(TEST_KEY)).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        when(lifecycleManager.start("tensor_od.csv", SOURCE_ID)).thenReturn(mockRun);
    }

    private OriginDestinationFlowCsvRow createValidRow() {
        return new OriginDestinationFlowCsvRow(
                "CBD_BEIRAMAR", "CENTRO",
                "Florianopolis", "Florianopolis",
                "-27.595400", "-48.548000", "-27.600000", "-48.550000",
                "true", "100", "50", "1.200", "MANHA"
        );
    }

    private OriginDestinationFlowCsvRow createRow(String originCluster, String period) {
        return new OriginDestinationFlowCsvRow(
                originCluster, "CENTRO",
                "Florianopolis", "Florianopolis",
                "-27.595400", "-48.548000", "-27.600000", "-48.550000",
                "true", "100", "50", "1.200", period
        );
    }

    private RegionIndex createRegionIndex(List<String> clusterNames) {
        var regions = new ArrayList<RegionEntity>();
        for (var clusterName : clusterNames) {
            regions.add(RegionEntity.builder().id(REGION_ID).clusterName(clusterName).municipality("Florianopolis").build());
        }
        return new RegionIndex(regions);
    }

    @Test
    void shouldIngestOdFlowsSuccessfully() throws Exception {
        mockSourceIdResolution();

        when(csvReader.read(any(InputStream.class), eq(OriginDestinationFlowCsvRow.class)))
                .thenReturn(createStubIterator(List.of(createValidRow())));
        when(regionResolver.regions()).thenReturn(createRegionIndex(List.of("CBD_BEIRAMAR", "CENTRO")));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isEqualTo(1);
        assertThat(result.rowsRejected()).isZero();
        verify(lifecycleManager).complete(mockRun, 1, 1, 0);

        var captor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
        verify(jdbcTemplate).batchUpdate(anyString(), captor.capture());

        var setter = captor.getValue();
        assertThat(setter.getBatchSize()).isEqualTo(1);

        var ps = mock(PreparedStatement.class);
        setter.setValues(ps, 0);
        verify(ps).setObject(1, ENTITY_ID);
        verify(ps).setObject(2, SOURCE_ID);
        verify(ps).setObject(3, REGION_ID);
        verify(ps).setObject(4, REGION_ID);
        verify(ps).setString(5, "CBD_BEIRAMAR");
        verify(ps).setString(6, "CENTRO");
        verify(ps).setString(7, "Florianopolis");
        verify(ps).setString(8, "Florianopolis");
        verify(ps).setBigDecimal(9, new BigDecimal("-27.595400"));
        verify(ps).setBigDecimal(10, new BigDecimal("-48.548000"));
        verify(ps).setBigDecimal(11, new BigDecimal("-27.600000"));
        verify(ps).setBigDecimal(12, new BigDecimal("-48.550000"));
        verify(ps).setString(13, "MANHA");
        verify(ps).setBoolean(14, true);
        verify(ps).setLong(15, 100L);
        verify(ps).setLong(16, 50L);
        verify(ps).setBigDecimal(17, new BigDecimal("1.200"));
    }

    @Test
    void shouldRejectDuplicateRowsInCsv() {
        mockSourceIdResolution();

        var row = createValidRow();
        when(csvReader.read(any(InputStream.class), eq(OriginDestinationFlowCsvRow.class)))
                .thenReturn(createStubIterator(List.of(row, row)));
        when(regionResolver.regions()).thenReturn(createRegionIndex(List.of("CBD_BEIRAMAR", "CENTRO")));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(2);
        assertThat(result.rowsInserted()).isEqualTo(1);
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldRejectInvalidPeriod() {
        mockSourceIdResolution();

        when(csvReader.read(any(InputStream.class), eq(OriginDestinationFlowCsvRow.class)))
                .thenReturn(createStubIterator(List.of(createRow("CBD_BEIRAMAR", "XUMLA"))));
        when(regionResolver.regions()).thenReturn(createRegionIndex(List.of("CBD_BEIRAMAR", "CENTRO")));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isZero();
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldRejectRowWhenRegionNotFound() {
        mockSourceIdResolution();

        when(csvReader.read(any(InputStream.class), eq(OriginDestinationFlowCsvRow.class)))
                .thenReturn(createStubIterator(List.of(createValidRow())));
        when(regionResolver.regions()).thenReturn(createRegionIndex(List.of()));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isZero();
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldRejectRowWithMalformedNumbers() {
        mockSourceIdResolution();

        var malformed = new OriginDestinationFlowCsvRow(
                "CBD_BEIRAMAR", "CENTRO",
                "Florianopolis", "Florianopolis",
                "-27.595400", "-48.548000", "-27.600000", "-48.550000",
                "true", "abc", "50", "1.200", "MANHA"
        );
        when(csvReader.read(any(InputStream.class), eq(OriginDestinationFlowCsvRow.class)))
                .thenReturn(createStubIterator(List.of(malformed)));
        when(regionResolver.regions()).thenReturn(createRegionIndex(List.of("CBD_BEIRAMAR", "CENTRO")));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isZero();
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldInsertInBatchesOfFiveHundred() {
        mockSourceIdResolution();

        var clusterNames = new ArrayList<String>();
        var rows = new ArrayList<OriginDestinationFlowCsvRow>();
        for (int i = 0; i < 501; i++) {
            clusterNames.add("CBD_BEIRAMAR-" + i);
            rows.add(createRow("CBD_BEIRAMAR-" + i, "MANHA"));
        }
        clusterNames.add("CENTRO");
        when(csvReader.read(any(InputStream.class), eq(OriginDestinationFlowCsvRow.class)))
                .thenReturn(createStubIterator(rows));
        when(regionResolver.regions()).thenReturn(createRegionIndex(clusterNames));

        CsvIngestResult result = service.ingest(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(501);
        assertThat(result.rowsInserted()).isEqualTo(501);
        verify(jdbcTemplate, times(2)).batchUpdate(anyString(), any(BatchPreparedStatementSetter.class));
    }

    @Test
    void shouldReturnEmptyResultWhenRunAlreadyCompleted() {
        mockSourceIdResolution();
        when(lifecycleManager.start("tensor_od.csv", SOURCE_ID)).thenReturn(null);

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
                .hasMessageContaining("tensor_od.csv");

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
