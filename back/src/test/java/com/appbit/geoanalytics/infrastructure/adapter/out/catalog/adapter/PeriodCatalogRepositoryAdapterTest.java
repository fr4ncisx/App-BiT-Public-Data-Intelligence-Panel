package com.appbit.geoanalytics.infrastructure.adapter.out.catalog.adapter;

import com.appbit.geoanalytics.infrastructure.adapter.out.concentration.repository.ConcentrationMetricJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeriodCatalogRepositoryAdapterTest {

    @Mock
    private ConcentrationMetricJpaRepository concentrationMetricJpaRepository;

    @InjectMocks
    private PeriodCatalogRepositoryAdapter adapter;

    @Test
    void returnsDistinctPeriods() {
        when(concentrationMetricJpaRepository.findDistinctPeriods())
                .thenReturn(List.of("MANHA", "TARDE", "NOITE"));

        List<String> result = adapter.findAll();

        assertThat(result).containsExactly("MANHA", "TARDE", "NOITE");
    }

    @Test
    void returnsEmptyWhenNoPeriods() {
        when(concentrationMetricJpaRepository.findDistinctPeriods()).thenReturn(List.of());

        List<String> result = adapter.findAll();

        assertThat(result).isEmpty();
    }
}
