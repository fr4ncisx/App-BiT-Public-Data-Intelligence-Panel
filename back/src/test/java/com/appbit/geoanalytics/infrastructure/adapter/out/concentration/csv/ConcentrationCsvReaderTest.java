package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.csv;

import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.csv.CsvMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcentrationCsvReaderTest {

    private final CsvMapper csvMapper = new CsvMapper();
    private final ConcentrationCsvReader reader = new ConcentrationCsvReader(csvMapper);

    @Test
    void shouldReadValidCsv() throws IOException {
        String csv = """
                ecgi,cluster,municipio,day_date,periodo,n_usuarios,n_sessoes,download_bytes,upload_bytes,dur_media_s,drop_pct_medio,congestionamento_medio,chamadas_total,mensagens_total,lat,lon
                1234567890123,CBD_BEIRAMAR,Florianopolis,2026-03-01,MANHA,100,50,1000000,500000,120,0.0100,0.050,10,5,-27.595400,-48.548000
                """;

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<ConcentrationCsvRow>();

        try (var iterator = reader.read(inputStream)) {
            while (iterator.hasNext()) {
                rows.add(iterator.next());
            }
        }

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().ecgi()).isEqualTo("1234567890123");
        assertThat(rows.getFirst().dayDate()).isEqualTo("2026-03-01");
    }

    @Test
    void shouldReadMultipleRows() throws IOException {
        String csv = """
                ecgi,cluster,municipio,day_date,periodo,n_usuarios,n_sessoes,download_bytes,upload_bytes,dur_media_s,drop_pct_medio,congestionamento_medio,chamadas_total,mensagens_total,lat,lon
                1234567890123,CBD_BEIRAMAR,Florianopolis,2026-03-01,MANHA,100,50,1000000,500000,120,0.0100,0.050,10,5,-27.595400,-48.548000
                1234567890124,TRINDADE,Florianopolis,2026-03-01,TARDE,200,80,2000000,800000,100,0.0200,0.100,20,10,-27.601100,-48.532000
                """;

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<ConcentrationCsvRow>();

        try (var iterator = reader.read(inputStream)) {
            while (iterator.hasNext()) {
                rows.add(iterator.next());
            }
        }

        assertThat(rows).hasSize(2);
    }

    @Test
    void shouldHandleEmptyCsv() throws IOException {
        String csv = "ecgi,cluster,municipio,day_date,periodo,n_usuarios,n_sessoes,download_bytes,upload_bytes,dur_media_s,drop_pct_medio,congestionamento_medio,chamadas_total,mensagens_total,lat,lon\n";

        InputStream inputStream = new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
        var rows = new ArrayList<ConcentrationCsvRow>();

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