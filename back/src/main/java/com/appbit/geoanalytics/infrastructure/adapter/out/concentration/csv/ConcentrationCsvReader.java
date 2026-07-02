package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.csv;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class ConcentrationCsvReader {

    private final CsvMapper csvMapper;

    public MappingIterator<ConcentrationCsvRow> read(InputStream inputStream) throws IOException {
        var schema = csvMapper.schemaFor(ConcentrationCsvRow.class)
                .withHeader()
                .withColumnReordering(true);

        return csvMapper.readerFor(ConcentrationCsvRow.class)
                .with(schema)
                .readValues(inputStream);
    }
}
