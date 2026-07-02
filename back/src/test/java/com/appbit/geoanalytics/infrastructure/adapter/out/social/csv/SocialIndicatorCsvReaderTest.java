package com.appbit.geoanalytics.infrastructure.adapter.out.social.csv;

import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.csv.CsvMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialIndicatorCsvReaderTest {

    private final CsvMapper csvMapper = new CsvMapper();
    private final SocialIndicatorCsvReader reader = new SocialIndicatorCsvReader(csvMapper);

    @Test
    void shouldReadValidCsv() throws IOException {
        String csv = """
                region_code,indicator_type,score,unit,gap_level,confidence_level,description
                TRINDADE,TRAINING,0.7200,PROGRAMS,MEDIUM,HIGH,Programas de formacion tecnologica disponibles en Trindade
                """;

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<SocialIndicatorCsvRow>();

        try (var iterator = reader.read(inputStream)) {
            while (iterator.hasNext()) {
                rows.add(iterator.next());
            }
        }

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().regionCode()).isEqualTo("TRINDADE");
        assertThat(rows.getFirst().indicatorType()).isEqualTo("TRAINING");
    }

    @Test
    void shouldReadMultipleRows() throws IOException {
        String csv = """
                region_code,indicator_type,score,unit,gap_level,confidence_level,description
                TRINDADE,TRAINING,0.7200,PROGRAMS,MEDIUM,HIGH,Programas de formacion tecnologica disponibles en Trindade
                CBD_BEIRAMAR,EMPLOYABILITY,0.7800,PERCENTAGE,LOW,HIGH,Tasa de insercion laboral formal en CBD Beiramar
                """;

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<SocialIndicatorCsvRow>();

        try (var iterator = reader.read(inputStream)) {
            while (iterator.hasNext()) {
                rows.add(iterator.next());
            }
        }

        assertThat(rows).hasSize(2);
    }

    @Test
    void shouldHandleEmptyCsv() throws IOException {
        String csv = "region_code,indicator_type,score,unit,gap_level,confidence_level,description\n";

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<SocialIndicatorCsvRow>();

        try (var iterator = reader.read(inputStream)) {
            while (iterator.hasNext()) {
                rows.add(iterator.next());
            }
        }

        assertThat(rows).isEmpty();
    }

    @Test
    void shouldThrowOnMalformedCsv() {
        String csv = "not_a_header\nvalue1,value2\n";
        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> {
            try (var iterator = reader.read(inputStream)) {
                while (iterator.hasNext()) {
                    iterator.next();
                }
            }
        }).isInstanceOf(Exception.class);
    }
}