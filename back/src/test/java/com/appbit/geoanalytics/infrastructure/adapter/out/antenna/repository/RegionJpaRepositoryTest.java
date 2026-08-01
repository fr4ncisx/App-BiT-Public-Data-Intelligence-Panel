package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.RegionEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RegionJpaRepositoryTest {

    @Autowired
    private RegionJpaRepository repository;

    private RegionEntity region(String regionCode) {
        return RegionEntity.builder()
                .id(UUID.randomUUID())
                .regionCode(regionCode)
                .regionName("Centro")
                .municipality("Florianopolis")
                .clusterName("CBD_BEIRAMAR")
                .centerLatitude(new BigDecimal("-27.595400"))
                .centerLongitude(new BigDecimal("-48.548000"))
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldInsertAndFindByRegionCode() {
        var region = region("SC-FLN-CBD");
        repository.saveAndFlush(region);

        var found = repository.findByRegionCode("SC-FLN-CBD");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(region.getId());
        assertThat(found.get().getRegionName()).isEqualTo("Centro");
    }

    @Test
    void shouldFindByClusterName() {
        repository.saveAndFlush(region("SC-FLN-CBD"));

        var found = repository.findByClusterName("CBD_BEIRAMAR");

        assertThat(found).isPresent();
        assertThat(found.get().getMunicipality()).isEqualTo("Florianopolis");
    }

    @Test
    void shouldFindByClusterNameAndMunicipality() {
        repository.saveAndFlush(region("SC-FLN-CBD"));

        var found = repository.findByClusterNameAndMunicipality("CBD_BEIRAMAR", "Florianopolis");

        assertThat(found).isPresent();
        assertThat(found.get().getRegionCode()).isEqualTo("SC-FLN-CBD");
    }

    @Test
    void shouldReturnEmptyWhenClusterAndMunicipalityDoNotMatch() {
        repository.saveAndFlush(region("SC-FLN-CBD"));

        var found = repository.findByClusterNameAndMunicipality("CBD_BEIRAMAR", "Sao Jose");

        assertThat(found).isEmpty();
    }
}
