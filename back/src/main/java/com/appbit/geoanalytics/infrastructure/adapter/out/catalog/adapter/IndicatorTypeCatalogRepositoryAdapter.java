package com.appbit.geoanalytics.infrastructure.adapter.out.catalog.adapter;

import com.appbit.geoanalytics.application.catalog.out.IndicatorTypeCatalogPort;
import com.appbit.geoanalytics.infrastructure.adapter.out.social.repository.SocialIndicatorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class IndicatorTypeCatalogRepositoryAdapter implements IndicatorTypeCatalogPort {

    private final SocialIndicatorJpaRepository socialIndicatorJpaRepository;

    @Override
    public List<String> findAll() {
        return socialIndicatorJpaRepository.findDistinctIndicatorTypes();
    }
}
