package com.appbit.geoanalytics.application.maps;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Flujos de movilidad entre regiones para visualización en mapa.")
public record FlowsResponse(
        @Schema(description = "Lista de flujos origen-destino entre regiones.")
        List<FlowDTO> flows
) {

    @Schema(description = "Flujo de movilidad entre una región de origen y una de destino.")
    public record FlowDTO(
            @Schema(description = "Identificador único.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            UUID id,

            @Schema(description = "ID de la región de origen.")
            UUID originRegionId,

            @Schema(description = "ID de la región de destino.")
            UUID destinationRegionId,

            @Schema(description = "Nombre del cluster de origen.", example = "CBD Beiramar")
            String originClusterName,

            @Schema(description = "Nombre del cluster de destino.", example = "Centro")
            String destinationClusterName,

            @Schema(description = "Municipio de origen.", example = "Florianopolis")
            String originMunicipality,

            @Schema(description = "Municipio de destino.", example = "Florianopolis")
            String destinationMunicipality,

            @Schema(description = "Coordenadas del punto de origen.")
            GeoPointDTO originPoint,

            @Schema(description = "Coordenadas del punto de destino.")
            GeoPointDTO destinationPoint,

            @Schema(description = "Indica si origen y destino están en el mismo cluster.", example = "false")
            Boolean sameCluster,

            @Schema(description = "Cantidad de usuarios.", example = "12500")
            Long usersCount,

            @Schema(description = "Cantidad de viajes.", example = "18750")
            Long tripsCount,

            @Schema(description = "Distancia media en km.", example = "5.3")
            BigDecimal averageDistanceKm,

            @Schema(description = "Período predominante.", example = "MANHA")
            String predominantPeriod
    ) {
    }

    public record GeoPointDTO(
            @Schema(description = "Latitud.", example = "-27.5954")
            BigDecimal lat,

            @Schema(description = "Longitud.", example = "-48.5480")
            BigDecimal lng
    ) {
    }
}
