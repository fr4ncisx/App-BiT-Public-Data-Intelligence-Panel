package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class RegionIndex {

    private final Map<String, RegionRef> byClusterName;

    public RegionIndex(Collection<RegionEntity> regions) {
        var index = new HashMap<String, RegionRef>();
        for (var region : regions) {
            var clusterName = region.getClusterName().trim();
            index.putIfAbsent(clusterName, new RegionRef(region.getId(), clusterName, region.getMunicipality()));
        }
        this.byClusterName = Map.copyOf(index);
    }

    public Optional<RegionRef> byClusterName(String clusterName) {
        return Optional.ofNullable(byClusterName.get(clusterName.trim()));
    }

    public int size() {
        return byClusterName.size();
    }
}
