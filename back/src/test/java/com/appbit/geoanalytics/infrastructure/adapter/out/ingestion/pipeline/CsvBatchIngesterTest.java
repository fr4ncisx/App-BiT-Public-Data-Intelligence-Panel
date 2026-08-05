package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline;

import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.MappingIterator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CsvBatchIngesterTest {

    @Mock private DatasetObjectStoragePort storagePort;
    @Mock private GenericCsvReader csvReader;
    @Mock private TransactionTemplate transactionTemplate;

    private CsvBatchIngester batchIngester;
    private static final DatasetObjectKey TEST_KEY = new DatasetObjectKey("test.csv");

    @BeforeEach
    void setUp() {
        when(storagePort.openStream(TEST_KEY))
                .thenReturn(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
        doAnswer(invocation -> {
            Consumer<TransactionStatus> action = invocation.getArgument(0);
            action.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
        batchIngester = new CsvBatchIngester(
                storagePort, csvReader, transactionTemplate, new IngestionProperties(true, 2, true));
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

    @Test
    void shouldSplitRowsIntoChunksOfConfiguredSize() {
        when(csvReader.read(any(InputStream.class), eq(String.class)))
                .thenReturn(createStubIterator(List.of("a", "b", "c", "d", "e")));

        var written = new ArrayList<List<String>>();
        CsvIngestResult result = batchIngester.ingest(
                TEST_KEY,
                String.class,
                Optional::of,
                written::add);

        assertThat(result.rowsRead()).isEqualTo(5);
        assertThat(result.rowsInserted()).isEqualTo(5);
        assertThat(result.rowsRejected()).isZero();
        assertThat(written).containsExactly(List.of("a", "b"), List.of("c", "d"), List.of("e"));
        verify(transactionTemplate, times(3)).executeWithoutResult(any());
    }

    @Test
    void shouldCountRejectedRowsWhenMapperReturnsEmpty() {
        when(csvReader.read(any(InputStream.class), eq(String.class)))
                .thenReturn(createStubIterator(List.of("a", "b", "c", "d", "e")));

        CsvIngestResult result = batchIngester.ingest(
                TEST_KEY,
                String.class,
                row -> "b".equals(row) || "c".equals(row) ? Optional.of(row) : Optional.empty(),
                _ -> {
                });

        assertThat(result.rowsRead()).isEqualTo(5);
        assertThat(result.rowsInserted()).isEqualTo(2);
        assertThat(result.rowsRejected()).isEqualTo(3);
    }

    @Test
    void shouldWrapStorageErrors() {
        when(storagePort.openStream(TEST_KEY)).thenThrow(new RuntimeException("Storage unavailable"));

        assertThatThrownBy(() -> batchIngester.ingest(TEST_KEY, String.class, Optional::of, _ -> {
        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("test.csv")
                .hasMessageContaining("Storage unavailable");
    }

    @Test
    void shouldPropagateWriterFailures() {
        when(csvReader.read(any(InputStream.class), eq(String.class)))
                .thenReturn(createStubIterator(List.of("a")));

        assertThatThrownBy(() -> batchIngester.ingest(
                TEST_KEY,
                String.class,
                Optional::of,
                _ -> {
                    throw new RuntimeException("Write failed");
                }))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Write failed");
    }
}