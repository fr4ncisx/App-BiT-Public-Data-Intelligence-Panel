package com.appbit.geoanalytics.infrastructure.adapter.out.maps.adapter;

import com.appbit.geoanalytics.application.maps.out.FlowSummary;
import com.appbit.geoanalytics.application.maps.out.MobilityFlowMapPort;
import com.appbit.geoanalytics.infrastructure.adapter.out.mobility.repository.OriginDestinationFlowJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MobilityFlowMapRepositoryAdapter implements MobilityFlowMapPort {

    private final OriginDestinationFlowJpaRepository odFlowRepository;

    @Override
    public List<FlowSummary> findFlowsByRegionIds(List<UUID> regionIds) {
        return odFlowRepository.findFlowsByRegionIds(regionIds).stream()
                .map(this::toSummary)
                .toList();
    }

    private FlowSummary toSummary(Object[] row) {
        return new FlowSummary(
                (UUID) row[0],
                (UUID) row[1],
                (String) row[2],
                (String) row[3],
                (String) row[4],
                (String) row[5],
                (BigDecimal) row[6],
                (BigDecimal) row[7],
                (BigDecimal) row[8],
                (BigDecimal) row[9],
                (Boolean) row[10],
                (Long) row[11],
                (Long) row[12],
                (BigDecimal) row[13],
                (String) row[14]
        );
    }
}
