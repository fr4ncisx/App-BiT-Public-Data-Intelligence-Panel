package com.appbit.geoanalytics.infrastructure.adapter.out.concentration.csv;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ConcentrationCsvRow(
        @JsonProperty("ecgi") String ecgi,
        @JsonProperty("cluster") String cluster,
        @JsonProperty("municipio") String municipio,
        @JsonProperty("day_date") String dayDate,
        @JsonProperty("periodo") String periodo,
        @JsonProperty("n_usuarios") String nUsuarios,
        @JsonProperty("n_sessoes") String nSessoes,
        @JsonProperty("download_bytes") String downloadBytes,
        @JsonProperty("upload_bytes") String uploadBytes,
        @JsonProperty("dur_media_s") String durMediaS,
        @JsonProperty("drop_pct_medio") String dropPctMedio,
        @JsonProperty("congestionamento_medio") String congestionamentoMedio,
        @JsonProperty("chamadas_total") String chamadasTotal,
        @JsonProperty("mensagens_total") String mensagensTotal,
        @JsonProperty("lat") String lat,
        @JsonProperty("lon") String lon
) {
}
