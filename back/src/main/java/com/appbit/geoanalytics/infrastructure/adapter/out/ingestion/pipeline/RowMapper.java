package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline;

import java.util.Optional;

@FunctionalInterface
public interface RowMapper<R, E> {

    Optional<E> map(R row);
}
