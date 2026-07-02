package com.appbit.geoanalytics.infrastructure.adapter.out.source.repository;

import com.appbit.geoanalytics.infrastructure.adapter.out.source.entity.DataSourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DataSourceJpaRepository extends JpaRepository<DataSourceEntity, UUID> {
    Optional<DataSourceEntity> findByFileName(String fileName);
}
