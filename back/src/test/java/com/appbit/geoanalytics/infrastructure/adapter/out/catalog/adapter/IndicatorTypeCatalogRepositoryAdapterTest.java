package com.appbit.geoanalytics.infrastructure.adapter.out.catalog.adapter;

import com.appbit.geoanalytics.infrastructure.adapter.out.social.repository.SocialIndicatorJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicatorTypeCatalogRepositoryAdapterTest {

    @Mock
    private SocialIndicatorJpaRepository socialIndicatorJpaRepository;

    @InjectMocks
    private IndicatorTypeCatalogRepositoryAdapter adapter;

    @Test
    void returnsDistinctIndicatorTypes() {
        when(socialIndicatorJpaRepository.findDistinctIndicatorTypes())
                .thenReturn(List.of("EMPLOYABILITY", "MENTAL_HEALTH", "TRAINING"));

        List<String> result = adapter.findAll();

        assertThat(result).containsExactly("EMPLOYABILITY", "MENTAL_HEALTH", "TRAINING");
    }

    @Test
    void returnsEmptyWhenNoIndicatorTypes() {
        when(socialIndicatorJpaRepository.findDistinctIndicatorTypes()).thenReturn(List.of());

        List<String> result = adapter.findAll();

        assertThat(result).isEmpty();
    }
}
