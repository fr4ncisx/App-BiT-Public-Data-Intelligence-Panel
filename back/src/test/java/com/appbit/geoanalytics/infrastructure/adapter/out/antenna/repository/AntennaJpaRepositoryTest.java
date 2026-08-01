package com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.entity.AntennaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AntennaJpaRepositoryTest {

    @Autowired
    private AntennaJpaRepository repository;

    private AntennaEntity antenna(String ecgi) {
        return AntennaEntity.builder()
                .id(UUID.randomUUID())
                .ecgi(ecgi)
                .regionId(UUID.randomUUID())
                .clusterName("CBD_BEIRAMAR")
                .municipality("Florianopolis")
                .latitude(new BigDecimal("-27.595400"))
                .longitude(new BigDecimal("-48.548000"))
                .sourceId(UUID.randomUUID())
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldInsertAndFind() {
        var antenna = antenna("1234567890123");
        repository.saveAndFlush(antenna);

        var found = repository.findById(antenna.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEcgi()).isEqualTo("1234567890123");
        assertThat(found.get().getClusterName()).isEqualTo("CBD_BEIRAMAR");
    }

    @Test
    void shouldFindByEcgiIn() {
        repository.saveAll(List.of(antenna("1234567890123"), antenna("4567890123456")));
        repository.flush();

        List<AntennaEntity> found = repository.findByEcgiIn(List.of("1234567890123", "9999999999999"));

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getEcgi()).isEqualTo("1234567890123");
    }

    @Test
    void shouldCheckExistsByEcgi() {
        repository.saveAndFlush(antenna("1234567890123"));

        assertThat(repository.existsByEcgi("1234567890123")).isTrue();
        assertThat(repository.existsByEcgi("9999999999999")).isFalse();
    }

    @Test
    void shouldFindAllEcgis() {
        repository.saveAll(List.of(antenna("1234567890123"), antenna("4567890123456")));
        repository.flush();

        Set<String> ecgis = repository.findAllEcgis();

        assertThat(ecgis).containsExactlyInAnyOrder("1234567890123", "4567890123456");
    }
}
