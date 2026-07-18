package com.appbit.geoanalytics.application.maps.out;

import java.util.List;
import java.util.UUID;

public interface MobilityFlowMapPort {

    List<FlowSummary> findFlowsByRegionIds(List<UUID> regionIds);
}
