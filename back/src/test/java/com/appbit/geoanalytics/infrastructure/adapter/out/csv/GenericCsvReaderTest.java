package com.appbit.geoanalytics.infrastructure.adapter.out.csv;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.csv.AntennaCsvRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.csv.CsvMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class GenericCsvReaderTest {

    private GenericCsvReader csvReader;

    @BeforeEach
    void setUp() {
        csvReader = new GenericCsvReader(new CsvMapper());
    }

    @Test
    void shouldReadCsvIntoMappingIterator() {
        String csvContent = """
                ecgi,cluster,municipio,lat,lon
                123456789012,CLUSTER_A,MUNICIPIO_1,-34.6037,-58.3816
                """;
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));

        var result = new ArrayList<AntennaCsvRow>();

        try (var iterator = csvReader.read(inputStream, AntennaCsvRow.class)) {
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        }

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().ecgi()).isEqualTo("123456789012");
        assertThat(result.getFirst().cluster()).isEqualTo("CLUSTER_A");
    }
}