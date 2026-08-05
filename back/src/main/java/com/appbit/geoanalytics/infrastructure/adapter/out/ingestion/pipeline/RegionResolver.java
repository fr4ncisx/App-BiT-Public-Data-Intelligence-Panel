package com.appbit.geoanalytics.infrastructure.adapter.out.ingestion.pipeline;

import com.appbit.geoanalytics.infrastructure.adapter.out.antenna.repository.RegionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RegionResolver {

    private final RegionJpaRepository regionRepository;
    private final AtomicInteger loadCount = new AtomicInteger();
    private volatile RegionIndex cached;

    public RegionIndex regions() {
        var current = cached;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (cached == null) {
                cached = load();
            }
            return cached;
        }
    }

    public int loadCount() {
        return loadCount.get();
    }

    private RegionIndex load() {
        loadCount.incrementAndGet();
        return new RegionIndex(regionRepository.findAll());
    }
}
