package com.appbit.geoanalytics.application.ingestion.in;

import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionTaskResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IngestionOrchestrator {

    CompletableFuture<List<IngestionTaskResult>> executeAll();
}
