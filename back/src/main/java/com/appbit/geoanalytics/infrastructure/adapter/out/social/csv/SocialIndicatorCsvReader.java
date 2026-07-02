package com.appbit.geoanalytics.infrastructure.adapter.out.social.csv;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class SocialIndicatorCsvReader {

    private final CsvMapper csvMapper;

    public MappingIterator<SocialIndicatorCsvRow> read(InputStream inputStream) throws IOException {
        var schema = csvMapper.schemaFor(SocialIndicatorCsvRow.class)
                .withHeader()
                .withColumnReordering(true);

        return csvMapper.readerFor(SocialIndicatorCsvRow.class)
                .with(schema)
                .readValues(inputStream);
    }
}
