package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.adapter;

import com.appbit.geoanalytics.application.antenna.in.AntennaIngestResult;
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
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.csv.AntennaCsvRow;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.AntennaJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.manager.IngestionLifecycleManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.MappingIterator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
class IngestAntennasServiceTest {

    @Mock private DatasetObjectStoragePort storagePort;
    @Mock private DataSourcePort dataSourcePort;
    @Mock private GenericCsvReader csvReader;
    @Mock private AntennaJpaRepository antennaRepository;
    @Mock private RegionJpaRepository regionRepository;
    @Mock private IngestionLifecycleManager lifecycleManager;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private IdGeneratorPort idGeneratorPort;

    private IngestAntennasService service;

    private static final DatasetObjectKey TEST_KEY = new DatasetObjectKey("antenas_flp.csv");
    private static final UUID SOURCE_ID = DomainFixtures.uuidV7();
    private IngestionRun mockRun;

    @BeforeEach
    void setUp() {
        when(idGeneratorPort.generate()).thenReturn(DomainFixtures.uuidV7());
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        service = new IngestAntennasService(
                storagePort, dataSourcePort, csvReader, antennaRepository, regionRepository,
                lifecycleManager, transactionTemplate, idGeneratorPort
        );

        mockRun = IngestionRun.builder()
                .id(new IngestionRunId(DomainFixtures.uuidV7()))
                .sourceId(new DataSourceId(SOURCE_ID))
                .fileName(new SourceFileName("antenas_flp.csv"))
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
        var entry = new SourceCatalogEntry(SOURCE_ID, "Vísent CDRView", new SourceFileName("antenas_flp.csv"), DataSourceType.SYNTHETIC_DATASET, "Desc");
        when(dataSourcePort.findByFileName(new SourceFileName("antenas_flp.csv"))).thenReturn(Optional.of(entry));
        when(lifecycleManager.start("antenas_flp.csv", SOURCE_ID)).thenReturn(mockRun);
    }

    @Test
    void shouldIngestAntennasWithCorrectSourceId() {
        mockSourceIdResolution();

        var row = new AntennaCsvRow("1234567890123", "CBD_BEIRAMAR", "Florianopolis", "-27.595400", "-48.548000");
        var region = RegionEntity.builder().id(UUID.randomUUID()).build();

        when(storagePort.openStream(TEST_KEY)).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        when(csvReader.read(any(InputStream.class), eq(AntennaCsvRow.class))).thenReturn(createStubIterator(List.of(row)));
        when(regionRepository.findByClusterNameAndMunicipality("CBD_BEIRAMAR", "Florianopolis")).thenReturn(Optional.of(region));

        AntennaIngestResult result = service.execute(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(1);
        assertThat(result.rowsInserted()).isEqualTo(1);
        verify(lifecycleManager).complete(mockRun, 1, 1, 0);
    }

    @Test
    void shouldRejectRowsWithEmptyEcgi() {
        mockSourceIdResolution();
        var row = new AntennaCsvRow("", "CBD_BEIRAMAR", "Florianopolis", "-27.595400", "-48.548000");

        when(storagePort.openStream(TEST_KEY)).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        when(csvReader.read(any(InputStream.class), eq(AntennaCsvRow.class))).thenReturn(createStubIterator(List.of(row)));

        AntennaIngestResult result = service.execute(TEST_KEY);

        assertThat(result.rowsInserted()).isEqualTo(0);
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldRejectDuplicateEcgiInCsv() {
        mockSourceIdResolution();

        var row1 = new AntennaCsvRow("1234567890123", "CBD_BEIRAMAR", "Florianopolis", "-27.595400", "-48.548000");
        var row2 = new AntennaCsvRow("1234567890123", "TRINDADE", "Florianopolis", "-27.601100", "-48.532000");
        var region = RegionEntity.builder().id(UUID.randomUUID()).build();

        when(storagePort.openStream(TEST_KEY)).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        when(csvReader.read(any(InputStream.class), eq(AntennaCsvRow.class))).thenReturn(createStubIterator(List.of(row1, row2)));
        when(regionRepository.findByClusterNameAndMunicipality(anyString(), anyString())).thenReturn(Optional.of(region));

        AntennaIngestResult result = service.execute(TEST_KEY);

        assertThat(result.rowsRead()).isEqualTo(2);
        assertThat(result.rowsInserted()).isEqualTo(1);
        assertThat(result.rowsRejected()).isEqualTo(1);
    }

    @Test
    void shouldCreateRegionWhenNotExists() {
        mockSourceIdResolution();

        var row = new AntennaCsvRow("1234567890123", "CBD_BEIRAMAR", "Florianopolis", "-27.595400", "-48.548000");
        var newRegion = RegionEntity.builder().id(UUID.randomUUID()).build();

        when(storagePort.openStream(TEST_KEY)).thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        when(csvReader.read(any(InputStream.class), eq(AntennaCsvRow.class))).thenReturn(createStubIterator(List.of(row)));
        when(regionRepository.findByClusterNameAndMunicipality("CBD_BEIRAMAR", "Florianopolis")).thenReturn(Optional.empty());
        when(regionRepository.save(any(RegionEntity.class))).thenReturn(newRegion);

        AntennaIngestResult result = service.execute(TEST_KEY);

        assertThat(result.rowsInserted()).isEqualTo(1);
        verify(regionRepository).save(any(RegionEntity.class));
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