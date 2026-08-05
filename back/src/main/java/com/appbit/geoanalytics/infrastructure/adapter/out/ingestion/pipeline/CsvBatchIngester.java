package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline;

import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.storage.dto.DatasetObjectKey;
import com.appbit.geoanalytics.application.storage.port.out.DatasetObjectStoragePort;
import com.appbit.geoanalytics.infrastructure.adapter.out.csv.GenericCsvReader;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CsvBatchIngester {

    private final DatasetObjectStoragePort storagePort;
    private final GenericCsvReader csvReader;
    private final TransactionTemplate transactionTemplate;
    private final IngestionProperties ingestionProperties;

    public <R, E> CsvIngestResult ingest(
            DatasetObjectKey key,
            Class<R> rowClass,
            RowMapper<R, E> rowMapper,
            BatchWriter<E> batchWriter) {
        var read = 0;
        var inserted = 0;
        var rejected = 0;
        var pending = new ArrayList<E>(ingestionProperties.batchSize());

        try (var inputStream = storagePort.openStream(key);
             var iterator = csvReader.read(inputStream, rowClass)) {

            while (iterator.hasNext()) {
                read++;
                var entity = rowMapper.map(iterator.next());
                if (entity.isPresent()) {
                    pending.add(entity.get());
                    if (pending.size() >= ingestionProperties.batchSize()) {
                        inserted += flush(pending, batchWriter);
                    }
                } else {
                    rejected++;
                }
            }

            if (!pending.isEmpty()) {
                inserted += flush(pending, batchWriter);
            }

            return new CsvIngestResult(read, inserted, rejected);

        } catch (Exception e) {
            throw new RuntimeException("Failed to ingest CSV stream '" + key.value() + "': " + e.getMessage(), e);
        }
    }

    private <E> int flush(List<E> pending, BatchWriter<E> batchWriter) {
        var chunk = List.copyOf(pending);
        pending.clear();
        transactionTemplate.executeWithoutResult(_ -> batchWriter.write(chunk));
        return chunk.size();
    }
}
