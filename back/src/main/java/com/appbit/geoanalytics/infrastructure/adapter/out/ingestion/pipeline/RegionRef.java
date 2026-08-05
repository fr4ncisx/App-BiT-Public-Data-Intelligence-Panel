package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline;

import java.util.UUID;

public record RegionRef(UUID id, String clusterName, String municipality) {
}