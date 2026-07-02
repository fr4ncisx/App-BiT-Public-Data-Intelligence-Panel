package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.csv;

import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.csv.CsvMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AntennaCsvReaderTest {

    private final CsvMapper csvMapper = new CsvMapper();
    private final AntennaCsvReader reader = new AntennaCsvReader(csvMapper);

    @Test
    void shouldReadValidCsv() throws IOException {
        String csv = """
                ecgi,cluster,municipio,lat,lon
                1234567890123,CBD_BEIRAMAR,Florianopolis,-27.595400,-48.548000
                1234567890124,TRINDADE,Florianopolis,-27.601100,-48.532000
                """;

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<AntennaCsvRow>();

        try (var iterator = reader.read(inputStream)) {
            while (iterator.hasNext()) {
                rows.add(iterator.next());
            }
        }

        assertThat(rows).hasSize(2);
        assertThat(rows.getFirst().ecgi()).isEqualTo("1234567890123");
        assertThat(rows.getFirst().cluster()).isEqualTo("CBD_BEIRAMAR");
    }

    @Test
    void shouldReadSingleRow() throws IOException {
        String csv = """
                ecgi,cluster,municipio,lat,lon
                9998887776661,UFSC,Florianopolis,-27.596900,-48.550000
                """;

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<AntennaCsvRow>();

        try (var iterator = reader.read(inputStream)) {
            while (iterator.hasNext()) {
                rows.add(iterator.next());
            }
        }

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().ecgi()).isEqualTo("9998887776661");
    }

    @Test
    void shouldHandleEmptyCsv() throws IOException {
        String csv = "ecgi,cluster,municipio,lat,lon\n";

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<AntennaCsvRow>();

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