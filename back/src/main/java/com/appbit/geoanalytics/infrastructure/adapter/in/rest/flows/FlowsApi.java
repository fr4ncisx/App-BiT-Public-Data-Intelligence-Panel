package com.appbit.geoanalytics.infrastructure.adapter.in.rest.flows;

import com.appbit.geoanalytics.application.maps.FlowsResponse;
import com.appbit.geoanalytics.infrastructure.adapter.in.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Maps", description = "Datos geoespaciales para visualización en mapa.")
@RequestMapping("/api/v1/maps")
public interface FlowsApi {

    @GetMapping("/flows")
    @Operation(summary = "Obtener flujos de movilidad", description = "Retorna los flujos origen-destino entre regiones para visualización en mapa.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Flujos recuperados exitosamente.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":true,\"code\":\"MAP_REGIONS_RETRIEVED\",\"message\":\"Flows retrieved successfully\",\"data\":{\"flows\":[{\"id\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"originRegionId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"destinationRegionId\":\"3fa85f64-5717-4562-b3fc-2c963f66afa6\",\"originClusterName\":\"CBD Beiramar\",\"destinationClusterName\":\"Centro\",\"originMunicipality\":\"Florianopolis\",\"destinationMunicipality\":\"Florianopolis\",\"originPoint\":{\"lat\":-27.5954,\"lng\":-48.548},\"destinationPoint\":{\"lat\":-27.5968,\"lng\":-48.5492},\"sameCluster\":false,\"usersCount\":12500,\"tripsCount\":18750,\"averageDistanceKm\":5.3,\"predominantPeriod\":\"MANHA\"}]},\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[]}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor.",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\"success\":false,\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal server error\",\"data\":null,\"meta\":{\"requestId\":\"req-abc-123\",\"timestamp\":\"2025-01-01T00:00:00Z\",\"apiVersion\":\"v1\"},\"errors\":[{\"field\":null,\"reason\":\"Unexpected error processing request\",\"rejectedValue\":null}]}")))
    })
    ResponseEntity<ApiResponse<FlowsResponse>> getFlows();
}
