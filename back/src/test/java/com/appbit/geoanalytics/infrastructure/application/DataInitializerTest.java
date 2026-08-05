package com.appbit.geoanalytics.infrastructure.application;

import com.appbit.geoanalytics.application.ingestion.in.IngestionOrchestrator;
import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionOutcome;
import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionTaskResult;
import com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.config.IngestionProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataInitializerTest {

    @Mock private IngestionOrchestrator orchestrator;
    @Mock private ApplicationContext applicationContext;

    private DataInitializer initializer;

    @BeforeEach
    void setUp() {
        when(applicationContext.getParent()).thenReturn(null);
        initializer = new DataInitializer(orchestrator, applicationContext, new IngestionProperties(true, 500, true));
    }

    @Test
    void skipsIngestionInChildContext() {
        when(applicationContext.getParent()).thenReturn(org.mockito.Mockito.mock(ApplicationContext.class));

        initializer.run(new DefaultApplicationArguments());

        verifyNoInteractions(orchestrator);
    }

    @Test
    void ingestsAllFilesSuccessfully() {
        var results = List.of(
                new IngestionTaskResult("tensor_concentracao.csv", true, IngestionOutcome.INGESTED, null, 10, 10, 0),
                new IngestionTaskResult("tensor_fluxo_vias.csv", true, IngestionOutcome.INGESTED, null, 5, 3, 2)
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        initializer.run(new DefaultApplicationArguments());
    }

    @Test
    void handlesAlreadyIngestedFilesWithoutThrowing() {
        var results = List.of(
                new IngestionTaskResult("tensor_concentracao.csv", true, IngestionOutcome.ALREADY_INGESTED, null, 0, 0, 0)
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        initializer.run(new DefaultApplicationArguments());
    }

    @Test
    void throwsWhenAnyIngestFails() {
        var results = List.of(
                new IngestionTaskResult("tensor_concentracao.csv", true, IngestionOutcome.INGESTED, null, 10, 10, 0),
                new IngestionTaskResult("tensor_od.csv", false, IngestionOutcome.FAILED, "Parse error", 0, 0, 0)
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tensor_od.csv: Parse error");
    }

    @Test
    void throwsWhenSkippedFileCountsAsFailure() {
        var results = List.of(
                new IngestionTaskResult("tensor_od.csv", false, IngestionOutcome.SKIPPED, "File not found in storage", 0, 0, 0)
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tensor_od.csv: File not found in storage");
    }

    @Test
    void throwsUsingUnknownErrorWhenErrorMessageMissing() {
        var results = List.of(
                new IngestionTaskResult("tensor_od.csv", false, IngestionOutcome.FAILED, null, 0, 0, 0)
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tensor_od.csv: Unknown error");
    }

    @Test
    void rethrowsRuntimeExceptionsFromOrchestration() {
        when(orchestrator.executeAll()).thenThrow(new IllegalStateException("Storage unavailable"));

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Storage unavailable");
    }

    @Test
    void skipsIngestionWhenDisabled() {
        initializer = new DataInitializer(orchestrator, applicationContext, new IngestionProperties(false, 500, true));

        initializer.run(new DefaultApplicationArguments());

        verifyNoInteractions(orchestrator);
    }

    @Test
    void doesNotThrowWhenFailFastDisabled() {
        initializer = new DataInitializer(orchestrator, applicationContext, new IngestionProperties(true, 500, false));
        var results = List.of(
                new IngestionTaskResult("tensor_od.csv", false, IngestionOutcome.FAILED, "Parse error", 0, 0, 0)
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        initializer.run(new DefaultApplicationArguments());
    }
}
