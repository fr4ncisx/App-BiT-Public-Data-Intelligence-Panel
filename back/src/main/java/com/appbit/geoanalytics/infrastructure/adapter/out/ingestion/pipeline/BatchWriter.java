package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline;

import java.util.List;

@FunctionalInterface
public interface BatchWriter<E> {

    void write(List<E> chunk);
}
