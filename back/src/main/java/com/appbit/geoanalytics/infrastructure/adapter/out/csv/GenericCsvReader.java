package com.appbit.geoanalytics.infrastructure.adapter.out.csv;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class GenericCsvReader {

    private final CsvMapper csvMapper;

    public <T> MappingIterator<T> read(InputStream inputStream, Class<T> type) {
        var schema = csvMapper.schemaFor(type)
                .withHeader()
                .withColumnReordering(true);

        return csvMapper.readerFor(type)
                .with(schema)
                .readValues(inputStream);
    }
}
