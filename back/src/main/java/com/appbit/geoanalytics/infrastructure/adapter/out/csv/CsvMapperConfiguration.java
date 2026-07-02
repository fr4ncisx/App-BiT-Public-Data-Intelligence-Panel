package com.appbit.geoanalytics.infrastructure.adapter.out.csv;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.dataformat.csv.CsvMapper;

@Configuration(proxyBeanMethods = false)
public class CsvMapperConfiguration {

    @Bean
    public CsvMapper csvMapper() {
        return new CsvMapper();
    }
}
