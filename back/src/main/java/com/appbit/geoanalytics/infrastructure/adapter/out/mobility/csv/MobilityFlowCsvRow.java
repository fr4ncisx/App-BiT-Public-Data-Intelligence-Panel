package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MobilityFlowCsvRow(
        @JsonProperty("ecgi_origem") String originEcgi,
        @JsonProperty("ecgi_destino") String destEcgi,
        @JsonProperty("cluster_origem") String originCluster,
        @JsonProperty("cluster_destino") String destCluster,
        @JsonProperty("municipio_origem") String originMunicipio,
        @JsonProperty("municipio_destino") String destMunicipio,
        @JsonProperty("lat_origem") String originLat,
        @JsonProperty("lon_origem") String originLon,
        @JsonProperty("lat_destino") String destLat,
        @JsonProperty("lon_destino") String destLon,
        @JsonProperty("n_usuarios") String nUsuarios,
        @JsonProperty("n_transicoes") String nTransicoes,
        @JsonProperty("dist_km") String distanciaKm,
        @JsonProperty("periodo_predominante") String periodo,
        @JsonProperty("pct_do_cluster_origem") String pctOrigemCluster
) {
}
