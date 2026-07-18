package com.appbit.geoanalytics.infrastructure.adapter.in.rest.config.usecase;

import com.appbit.geoanalytics.application.ai.in.AuditQueryService;
import com.appbit.geoanalytics.application.ai.in.AuditQueryUseCase;
import com.appbit.geoanalytics.application.ai.in.ClassifyIntentUseCase;
import com.appbit.geoanalytics.application.ai.in.GenerateAIAnswerService;
import com.appbit.geoanalytics.application.ai.in.GenerateAIAnswerUseCase;
import com.appbit.geoanalytics.application.ai.in.IntentClassifierService;
import com.appbit.geoanalytics.application.ai.in.RetrieveEvidenceService;
import com.appbit.geoanalytics.application.ai.in.RetrieveEvidenceUseCase;
import com.appbit.geoanalytics.application.ai.out.AiAuditPort;
import com.appbit.geoanalytics.application.catalog.in.GetCatalogService;
import com.appbit.geoanalytics.application.catalog.in.GetCatalogUseCase;
import com.appbit.geoanalytics.application.catalog.out.IndicatorTypeCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.PeriodCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.RegionCatalogPort;
import com.appbit.geoanalytics.application.catalog.out.SourceCatalogPort;
import com.appbit.geoanalytics.application.ingestion.out.IngestionRunPort;
import com.appbit.geoanalytics.application.maps.in.GetFlowsService;
import com.appbit.geoanalytics.application.maps.in.GetFlowsUseCase;
import com.appbit.geoanalytics.application.maps.in.CompareRegionsService;
import com.appbit.geoanalytics.application.maps.in.CompareRegionsUseCase;
import com.appbit.geoanalytics.application.maps.in.GetRegionsMapService;
import com.appbit.geoanalytics.application.maps.in.GetRegionsMapUseCase;
import com.appbit.geoanalytics.application.maps.out.ConcentrationMapPort;
import com.appbit.geoanalytics.application.maps.out.MobilityFlowMapPort;
import com.appbit.geoanalytics.application.maps.out.NetworkIndicatorMapPort;
import com.appbit.geoanalytics.application.maps.out.RegionMapPort;
import com.appbit.geoanalytics.application.maps.out.SocialIndicatorMapPort;
import com.appbit.geoanalytics.application.ranking.in.GetRegionRankingService;
import com.appbit.geoanalytics.application.ranking.in.GetRegionRankingUseCase;
import com.appbit.geoanalytics.application.social.in.GetSocialGapService;
import com.appbit.geoanalytics.application.social.in.GetSocialGapUseCase;
import com.appbit.geoanalytics.application.social.out.SocialGapPort;
import com.appbit.geoanalytics.application.source.in.FindDataSourceByFileNameService;
import com.appbit.geoanalytics.application.source.in.FindDataSourceByFileNameUseCase;
import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.sources.in.GetSourcesService;
import com.appbit.geoanalytics.application.sources.in.GetSourcesUseCase;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

    @Bean
    public GetCatalogUseCase getCatalogUseCase(
            RegionCatalogPort regionCatalogPort,
            SourceCatalogPort sourceCatalogPort,
            PeriodCatalogPort periodCatalogPort,
            IndicatorTypeCatalogPort indicatorTypeCatalogPort
    ) {
        return new GetCatalogService(regionCatalogPort, sourceCatalogPort, periodCatalogPort, indicatorTypeCatalogPort);
    }

    @Bean
    public GetSourcesUseCase getSourcesUseCase(
            DataSourcePort dataSourcePort,
            IngestionRunPort ingestionRunPort
    ) {
        return new GetSourcesService(dataSourcePort, ingestionRunPort);
    }

    @Bean
    public GetRegionRankingUseCase getRegionRankingUseCase(SocialGapPort socialGapPort) {
        return new GetRegionRankingService(socialGapPort);
    }

    @Bean
    public FindDataSourceByFileNameUseCase findDataSourceByFileNameUseCase(DataSourcePort dataSourcePort) {
        return new FindDataSourceByFileNameService(dataSourcePort);
    }

    @Bean
    public GetSocialGapUseCase getSocialGapUseCase(
            SocialGapPort socialGapPort,
            ConcentrationMapPort concentrationMapPort,
            NetworkIndicatorMapPort networkIndicatorMapPort
    ) {
        return new GetSocialGapService(socialGapPort, concentrationMapPort, networkIndicatorMapPort);
    }

    @Bean
    public GetFlowsUseCase getFlowsUseCase(
            MobilityFlowMapPort mobilityFlowMapPort,
            RegionMapPort regionMapPort
    ) {
        return new GetFlowsService(mobilityFlowMapPort, regionMapPort);
    }

    @Bean
    public GetRegionsMapUseCase getRegionsMapUseCase(
            RegionMapPort regionMapPort,
            ConcentrationMapPort concentrationMapPort,
            SocialIndicatorMapPort socialIndicatorMapPort
    ) {
        return new GetRegionsMapService(regionMapPort, concentrationMapPort, socialIndicatorMapPort);
    }

    @Bean
    public CompareRegionsUseCase compareRegionsUseCase(
            RegionMapPort regionMapPort,
            ConcentrationMapPort concentrationMapPort,
            SocialIndicatorMapPort socialIndicatorMapPort,
            NetworkIndicatorMapPort networkIndicatorMapPort
    ) {
        return new CompareRegionsService(regionMapPort, concentrationMapPort, socialIndicatorMapPort, networkIndicatorMapPort);
    }

    @Bean
    public RetrieveEvidenceUseCase retrieveEvidenceUseCase(
            RegionMapPort regionMapPort,
            ConcentrationMapPort concentrationMapPort,
            SocialIndicatorMapPort socialIndicatorMapPort,
            NetworkIndicatorMapPort networkIndicatorMapPort
    ) {
        return new RetrieveEvidenceService(regionMapPort, concentrationMapPort, socialIndicatorMapPort, networkIndicatorMapPort);
    }

    @Bean
    public ClassifyIntentUseCase classifyIntentUseCase() {
        return new IntentClassifierService();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.ai.chat.client.enabled", havingValue = "true", matchIfMissing = true)
    public GenerateAIAnswerUseCase generateAIAnswerUseCase(ChatClient.Builder chatClientBuilder) {
        return new GenerateAIAnswerService(chatClientBuilder);
    }

    @Bean
    public AuditQueryUseCase auditQueryUseCase(AiAuditPort aiAuditPort) {
        return new AuditQueryService(aiAuditPort);
    }
}
