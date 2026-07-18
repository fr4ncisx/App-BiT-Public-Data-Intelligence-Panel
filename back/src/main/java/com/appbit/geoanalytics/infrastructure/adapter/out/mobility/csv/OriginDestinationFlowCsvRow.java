package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OriginDestinationFlowCsvRow(
        @JsonProperty("cluster_origem") String originCluster,
        @JsonProperty("cluster_destino") String destCluster,
        @JsonProperty("municipio_origem") String originMunicipio,
        @JsonProperty("municipio_destino") String destMunicipio,
        @JsonProperty("lat_origem") String originLat,
        @JsonProperty("lon_origem") String originLon,
        @JsonProperty("lat_destino") String destLat,
        @JsonProperty("lon_destino") String destLon,
        @JsonProperty("mesmo_cluster") String mesmaCluster,
        @JsonProperty("n_usuarios") String nUsuarios,
        @JsonProperty("n_viagens") String nViagens,
        @JsonProperty("dist_media_km") String distanciaMediaKm,
        @JsonProperty("periodo_predominante") String periodo
) {
}
