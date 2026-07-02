package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.csv;

import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class AntennaCsvReader {

    private final CsvMapper csvMapper;

    public MappingIterator<AntennaCsvRow> read(InputStream inputStream) throws IOException {
        var schema = csvMapper.schemaFor(AntennaCsvRow.class)
                .withHeader()
                .withColumnReordering(true);

        return csvMapper.readerFor(AntennaCsvRow.class)
                .with(schema)
                .readValues(inputStream);
    }
}