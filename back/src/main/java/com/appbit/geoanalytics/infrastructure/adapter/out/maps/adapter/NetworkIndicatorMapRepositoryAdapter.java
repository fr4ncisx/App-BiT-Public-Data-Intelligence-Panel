package com.appbit.geoanalytics.infrastructure.adapter.out.maps.adapter;

import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorSummary;
import com.appbit.geoanalytics.infrastructure.adapter.out.network.repository.NetworkIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NetworkIndicatorMapRepositoryAdapter implements NetworkIndicatorMapPort {

    private final NetworkIndicatorJpaRepository networkIndicatorJpaRepository;

    @Override
    public List<NetworkIndicatorSummary> findByRegionIds(List<UUID> regionIds) {
        var results = networkIndicatorJpaRepository.findNetworkIndicatorsByRegionIds(regionIds);

        return results.stream()
                .map(row -> new NetworkIndicatorSummary(
                        (UUID) row[0],
                        (String) row[1],
                        (BigDecimal) row[2],
                        (String) row[3]))
                .toList();
    }
}
