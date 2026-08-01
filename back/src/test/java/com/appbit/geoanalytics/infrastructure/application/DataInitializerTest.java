package com.appbit.geoanalytics.infrastructure.application;

import com.appbit.geoanalytics.application.ingestion.in.dto.CsvIngestResult;
import com.appbit.geoanalytics.application.ingestion.in.dto.IngestionTaskResult;
import com.appbit.geoanalytics.application.ingestion.in.IngestionOrchestrator;
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
        initializer = new DataInitializer(orchestrator, applicationContext);
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
                IngestionTaskResult.success("tensor_concentracao.csv",
                        new CsvIngestResult(10, 10, 0)),
                IngestionTaskResult.success("tensor_fluxo_vias.csv",
                        new CsvIngestResult(5, 3, 2))
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        initializer.run(new DefaultApplicationArguments());
    }

    @Test
    void handlesAlreadyIngestedFilesWithZeroCounters() {
        var results = List.of(
                IngestionTaskResult.success("tensor_concentracao.csv",
                        new CsvIngestResult(0, 0, 0))
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        initializer.run(new DefaultApplicationArguments());
    }

    @Test
    void throwsWhenAnyIngestFails() {
        var results = List.of(
                IngestionTaskResult.success("tensor_concentracao.csv",
                        new CsvIngestResult(10, 10, 0)),
                IngestionTaskResult.failed("tensor_od.csv", "Parse error")
        );
        when(orchestrator.executeAll()).thenReturn(CompletableFuture.completedFuture(results));

        assertThatThrownBy(() -> initializer.run(new DefaultApplicationArguments()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("tensor_od.csv: Parse error");
    }

    @Test
    void throwsUsingUnknownErrorWhenErrorMessageMissing() {
        var results = List.of(
                IngestionTaskResult.failed("tensor_od.csv", null)
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
}
