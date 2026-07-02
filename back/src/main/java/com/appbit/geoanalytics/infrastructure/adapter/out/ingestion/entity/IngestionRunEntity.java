package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.entity;

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
@Table(name = "ingestion_runs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class IngestionRunEntity {

    @Id
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "file_name", nullable = false, length = 120)
    private String fileName;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "rows_read", nullable = false)
    private long rowsRead;

    @Column(name = "rows_inserted", nullable = false)
    private long rowsInserted;

    @Column(name = "rows_rejected", nullable = false)
    private long rowsRejected;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}
