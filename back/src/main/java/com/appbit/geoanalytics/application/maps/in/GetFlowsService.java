package com.appbit.geoanalytics.application.maps.in;

import com.appbit.geoanalytics.application.maps.FlowsResponse;
import com.appbit.geoanalytics.application.maps.FlowsResponse.FlowDTO;
import com.appbit.geoanalytics.application.maps.FlowsResponse.GeoPointDTO;
import com.appbit.geoanalytics.application.maps.out.MobilityFlowMapPort;
import com.appbit.geoanalytics.application.maps.out.RegionMapPort;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class GetFlowsService implements GetFlowsUseCase {

    private final MobilityFlowMapPort mobilityFlowMapPort;
    private final RegionMapPort regionMapPort;

    @Override
    public FlowsResponse execute() {
        var allRegions = regionMapPort.findAll();
        var regionIds = allRegions.stream().map(r -> r.id()).toList();
        var flows = mobilityFlowMapPort.findFlowsByRegionIds(regionIds);
        var dtos = flows.stream()
                .map(f -> new FlowDTO(
                        UUID.randomUUID(),
                        f.originRegionId(),
                        f.destinationRegionId(),
                        f.originClusterName(),
                        f.destinationClusterName(),
                        f.originMunicipality(),
                        f.destinationMunicipality(),
                        new GeoPointDTO(f.originLatitude(), f.originLongitude()),
                        new GeoPointDTO(f.destinationLatitude(), f.destinationLongitude()),
                        f.sameCluster(),
                        f.usersCount(),
                        f.tripsCount(),
                        f.averageDistanceKm(),
                        f.predominantPeriod()))
                .toList();
        return new FlowsResponse(dtos);
    }
}
