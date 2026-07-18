package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.entity.IngestionRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IngestionRunJpaRepository extends JpaRepository<IngestionRunEntity, UUID> {

    Optional<IngestionRunEntity> findFirstBySourceIdAndFileNameOrderByStartedAtDesc(
            @Param("sourceId") UUID sourceId,
            @Param("fileName") String fileName
    );
}
