package com.appbit.geoanalytics.infrastructure.adapter.out.source.adapter;

import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.entity.DataSourceEntity;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.mapper.DataSourceMapper;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.mapper.DataSourceMapperImpl;
import com.appbit.geoanalytics.infrastructure.adapter.out.source.repository.DataSourceJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(DataSourceMapperImpl.class)
class DataSourceRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DataSourceJpaRepository repository;

    @Autowired
    private DataSourceMapper mapper;

    private DataSourceRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DataSourceRepositoryAdapter(repository, mapper);
    }

    @Test
    void shouldFindDataSourceByFileName() {
        DataSourceEntity entity = DataSourceEntity.builder()
                .id(UUID.randomUUID())
                .sourceName("Vísent CDRView - Antennas")
                .fileName("antenas_flp.csv")
                .sourceType("SYNTHETIC_DATASET")
                .description("Antenas, coordenadas, municipio y cluster.")
                .createdAt(Instant.now())
                .build();

        entityManager.persistAndFlush(entity);

        Optional<SourceCatalogEntry> result = adapter.findByFileName(
                new SourceFileName("antenas_flp.csv")
        );

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(entity.getId());
        assertThat(result.get().sourceName()).isEqualTo("Vísent CDRView - Antennas");
        assertThat(result.get().fileName()).isEqualTo(new SourceFileName("antenas_flp.csv"));
        assertThat(result.get().sourceType()).isEqualTo(DataSourceType.SYNTHETIC_DATASET);
        assertThat(result.get().description()).isEqualTo("Antenas, coordenadas, municipio y cluster.");
    }

    @Test
    void shouldReturnEmptyWhenFileNotFound() {
        Optional<SourceCatalogEntry> result = adapter.findByFileName(
                new SourceFileName("nonexistent.csv")
        );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldMapSourceTypeStringToEnum() {
        DataSourceEntity entity = DataSourceEntity.builder()
                .id(UUID.randomUUID())
                .sourceName("Test Source")
                .fileName("test_data.csv")
                .sourceType("SYNTHETIC_DATASET")
                .description("Test description.")
                .createdAt(Instant.now())
                .build();

        entityManager.persistAndFlush(entity);

        SourceCatalogEntry result = adapter.findByFileName(
                new SourceFileName("test_data.csv")
        ).orElseThrow();

        assertThat(result.sourceType()).isEqualTo(DataSourceType.SYNTHETIC_DATASET);
    }

    @Test
    void shouldMapFileNameStringToSourceFileName() {
        DataSourceEntity entity = DataSourceEntity.builder()
                .id(UUID.randomUUID())
                .sourceName("Test Source")
                .fileName("mapping_test.csv")
                .sourceType("SYNTHETIC_DATASET")
                .description("Test description.")
                .createdAt(Instant.now())
                .build();

        entityManager.persistAndFlush(entity);

        SourceCatalogEntry result = adapter.findByFileName(
                new SourceFileName("mapping_test.csv")
        ).orElseThrow();

        assertThat(result.fileName()).isInstanceOf(SourceFileName.class);
        assertThat(result.fileName().value()).isEqualTo("mapping_test.csv");
    }

    @Test
    void shouldReturnAllDataSources() {
        DataSourceEntity entity1 = DataSourceEntity.builder()
                .id(UUID.randomUUID())
                .sourceName("Source 1")
                .fileName("file1.csv")
                .sourceType("SYNTHETIC_DATASET")
                .description("First file.")
                .createdAt(Instant.now())
                .build();

        DataSourceEntity entity2 = DataSourceEntity.builder()
                .id(UUID.randomUUID())
                .sourceName("Source 2")
                .fileName("file2.csv")
                .sourceType("SEED_DATA")
                .description("Second file.")
                .createdAt(Instant.now())
                .build();

        entityManager.persistAndFlush(entity1);
        entityManager.persistAndFlush(entity2);

        List<SourceCatalogEntry> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(SourceCatalogEntry::fileName)
                .extracting(SourceFileName::value)
                .containsExactlyInAnyOrder("file1.csv", "file2.csv");
    }

    @Test
    void shouldReturnEmptyListWhenNoDataSources() {
        List<SourceCatalogEntry> result = adapter.findAll();

        assertThat(result).isEmpty();
    }
}
