package com.appbit.geoanalytics.infrastructure.adapter.out.ai.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.ai.entity.AiQueryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiQueryJpaRepository extends JpaRepository<AiQueryEntity, UUID> {
}
