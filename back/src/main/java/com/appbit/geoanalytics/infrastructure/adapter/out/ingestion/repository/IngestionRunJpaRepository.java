package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.entity.IngestionRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngestionRunJpaRepository extends JpaRepository<IngestionRunEntity, UUID> {
}
