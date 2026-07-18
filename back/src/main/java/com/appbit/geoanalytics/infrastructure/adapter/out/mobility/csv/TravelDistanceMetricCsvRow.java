package com.appbit.geoanalytics.infrastructure.adapter.out.mobility.csv;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TravelDistanceMetricCsvRow(
        @JsonProperty("cluster_origem") String originCluster,
        @JsonProperty("cluster_destino") String destCluster,
        @JsonProperty("mesmo_cluster") String mesmaCluster,
        @JsonProperty("n_observacoes") String nObservacoes,
        @JsonProperty("dist_media_km") String distanciaMediaKm,
        @JsonProperty("dist_p25_km") String p25DistanciaKm,
        @JsonProperty("dist_p75_km") String p75DistanciaKm,
        @JsonProperty("periodo_predominante") String periodo
) {
}
