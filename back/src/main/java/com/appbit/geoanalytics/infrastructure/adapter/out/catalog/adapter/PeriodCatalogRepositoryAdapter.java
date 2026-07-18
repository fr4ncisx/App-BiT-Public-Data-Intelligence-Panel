package com.appbit.geoanalytics.infrastructure.adapter.out.catalog.adapter;

import com.appbit.geoanalytics.application.catalog.out.PeriodCatalogPort;
import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.repository.ConcentrationMetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PeriodCatalogRepositoryAdapter implements PeriodCatalogPort {

    private final ConcentrationMetricJpaRepository concentrationMetricJpaRepository;

    @Override
    public List<String> findAll() {
        return concentrationMetricJpaRepository.findDistinctPeriods();
    }
}
