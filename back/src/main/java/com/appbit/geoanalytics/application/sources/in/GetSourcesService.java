package com.appbit.geoanalytics.application.sources.in;

import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.application.sources.SourceDTO;
import com.appbit.geoanalytics.application.sources.SourcesResponse;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GetSourcesService implements GetSourcesUseCase {

    private final DataSourcePort dataSourcePort;
    private final IngestionRunPort ingestionRunPort;

    @Override
    public SourcesResponse execute() {
        List<SourceCatalogEntry> sources = dataSourcePort.findAll();

        List<SourceDTO> sourceDTOs = sources.stream()
                .map(this::toSourceDTO)
                .toList();

        return new SourcesResponse(sourceDTOs);
    }

    private SourceDTO toSourceDTO(SourceCatalogEntry source) {
        Optional<IngestionRun> latestRun = ingestionRunPort.findLatestBySourceIdAndFileName(
                source.id(),
                source.fileName().value());

        return new SourceDTO(
                source.sourceName(),
                source.fileName().value(),
                source.sourceType().name(),
                source.description(),
                source.confidenceLevel(),
                source.periodStart(),
                source.periodEnd(),
                source.governanceType(),
                latestRun.map(r -> r.getState().name()).orElse("PENDING"),
                latestRun.map(IngestionRun::getStartedAt).orElse(null),
                latestRun.flatMap(r -> Optional.ofNullable(r.getFinishedAt())).orElse(null),
                latestRun.map(IngestionRun::getRowsRead).orElse(0L),
                latestRun.map(IngestionRun::getRowsInserted).orElse(0L),
                latestRun.map(IngestionRun::getRowsRejected).orElse(0L),
                latestRun.map(IngestionRun::getErrorMessage).orElse(null)
        );
    }
}
