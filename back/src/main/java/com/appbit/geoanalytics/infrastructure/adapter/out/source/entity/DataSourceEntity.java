package com.appbit.geoanalytics.infrastructure.adapter.out.source.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "data_sources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DataSourceEntity {

    @Id
    private UUID id;

    @Column(name = "source_name", nullable = false, length = 120)
    private String sourceName;

    @Column(name = "file_name", nullable = false, length = 120)
    private String fileName;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
