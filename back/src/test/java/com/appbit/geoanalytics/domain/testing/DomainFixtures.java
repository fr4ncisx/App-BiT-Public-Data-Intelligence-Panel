package com.appbit.geoanalytics.domain.testing;

import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import com.appbit.geoanalytics.domain.ai.enums.Language;
import com.appbit.geoanalytics.domain.ai.enums.VisualizationType;
import com.appbit.geoanalytics.domain.ai.model.AiAnswer;
import com.appbit.geoanalytics.domain.ai.model.AiQuery;
import com.appbit.geoanalytics.domain.ai.vo.AiAnswerId;
import com.appbit.geoanalytics.domain.ai.vo.AiQueryId;
import com.appbit.geoanalytics.domain.ai.vo.AnswerEvidence;
import com.appbit.geoanalytics.domain.ai.vo.AnswerExplanation;
import com.appbit.geoanalytics.domain.ai.vo.AnswerSummary;
import com.appbit.geoanalytics.domain.ai.vo.AnswerWarning;
import com.appbit.geoanalytics.domain.ai.vo.QueryText;
import com.appbit.geoanalytics.domain.concentration.model.ConcentrationMetric;
import com.appbit.geoanalytics.domain.concentration.vo.ConcentrationMetricId;
import com.appbit.geoanalytics.domain.concentration.vo.MetricRatio;
import com.appbit.geoanalytics.domain.ingestion.enums.IngestionState;
import com.appbit.geoanalytics.domain.ingestion.model.IngestionRun;
import com.appbit.geoanalytics.domain.ingestion.vo.IngestionRunId;
import com.appbit.geoanalytics.domain.mobility.model.MobilityFlow;
import com.appbit.geoanalytics.domain.mobility.model.OriginDestinationFlow;
import com.appbit.geoanalytics.domain.mobility.model.TravelDistanceMetric;
import com.appbit.geoanalytics.domain.mobility.vo.FlowDistanceKm;
import com.appbit.geoanalytics.domain.mobility.vo.FlowPercentage;
import com.appbit.geoanalytics.domain.mobility.vo.MobilityFlowId;
import com.appbit.geoanalytics.domain.mobility.vo.OriginDestinationFlowId;
import com.appbit.geoanalytics.domain.mobility.vo.TravelDistanceMetricId;
import com.appbit.geoanalytics.domain.network.enums.NetworkIndicatorType;
import com.appbit.geoanalytics.domain.network.model.Antenna;
import com.appbit.geoanalytics.domain.network.model.NetworkIndicator;
import com.appbit.geoanalytics.domain.network.vo.AntennaId;
import com.appbit.geoanalytics.domain.network.vo.NetworkIndicatorId;
import com.appbit.geoanalytics.domain.privacy.model.PrivacySummary;
import com.appbit.geoanalytics.domain.privacy.vo.PrivacyParameter;
import com.appbit.geoanalytics.domain.privacy.vo.PrivacySummaryId;
import com.appbit.geoanalytics.domain.privacy.vo.PrivacyValue;
import com.appbit.geoanalytics.domain.region.model.Region;
import com.appbit.geoanalytics.domain.region.vo.RegionCode;
import com.appbit.geoanalytics.domain.region.vo.RegionId;
import com.appbit.geoanalytics.domain.shared.enums.ConfidenceLevel;
import com.appbit.geoanalytics.domain.shared.enums.GapLevel;
import com.appbit.geoanalytics.domain.shared.enums.IndicatorUnit;
import com.appbit.geoanalytics.domain.shared.enums.PeriodType;
import com.appbit.geoanalytics.domain.shared.vo.Ecgi;
import com.appbit.geoanalytics.domain.shared.vo.GeoPoint;
import com.appbit.geoanalytics.domain.shared.vo.IndicatorScore;
import com.appbit.geoanalytics.domain.shared.vo.Period;
import com.appbit.geoanalytics.domain.social.enums.SocialIndicatorType;
import com.appbit.geoanalytics.domain.social.model.SocialIndicator;
import com.appbit.geoanalytics.domain.social.vo.SocialIndicatorId;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.model.DataSource;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class DomainFixtures {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TIMESTAMP_BITS = 48;
    private static final int VERSION_BITS = 4;
    private static final int RANDOM_A_BITS = 12;
    private static final int RANDOM_B_BITS = 62;
    private static final int UUID_VERSION = 7;
    private static final int RFC_4122_VARIANT = 0b10;
    private static final long TIMESTAMP_MASK = (1L << TIMESTAMP_BITS) - 1;
    private static final long RANDOM_A_BOUND = 1L << RANDOM_A_BITS;
    private static final long RANDOM_B_BOUND = 1L << RANDOM_B_BITS;
    private static final long VERSION_VALUE = (long) UUID_VERSION << RANDOM_A_BITS;
    private static final long VARIANT_VALUE = (long) RFC_4122_VARIANT << RANDOM_B_BITS;

    private DomainFixtures() {
    }

    public static UUID uuidV7() {
        long timestamp = System.currentTimeMillis() & TIMESTAMP_MASK;
        long randomA = SECURE_RANDOM.nextLong(RANDOM_A_BOUND);
        long randomB = SECURE_RANDOM.nextLong(RANDOM_B_BOUND);

        long mostSignificantBits = (timestamp << (VERSION_BITS + RANDOM_A_BITS)) | VERSION_VALUE | randomA;
        long leastSignificantBits = VARIANT_VALUE | randomB;

        return new UUID(mostSignificantBits, leastSignificantBits);
    }

    public static UUID nilUuid() {
        return new UUID(0L, 0L);
    }

    public static UUID uuidV4() {
        return UUID.randomUUID();
    }

    public static UUID nonRfc4122UuidV7() {
        return new UUID(0x0000000000007000L, 0x0000000000000001L);
    }

    public static String text(int length) {
        return "x".repeat(length);
    }

    public static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    public static AiQueryId aiQueryId() {
        return new AiQueryId(uuidV7());
    }

    public static AiAnswerId aiAnswerId() {
        return new AiAnswerId(uuidV7());
    }

    public static ConcentrationMetricId concentrationMetricId() {
        return new ConcentrationMetricId(uuidV7());
    }

    public static IngestionRunId ingestionRunId() {
        return new IngestionRunId(uuidV7());
    }

    public static MobilityFlowId mobilityFlowId() {
        return new MobilityFlowId(uuidV7());
    }

    public static OriginDestinationFlowId originDestinationFlowId() {
        return new OriginDestinationFlowId(uuidV7());
    }

    public static TravelDistanceMetricId travelDistanceMetricId() {
        return new TravelDistanceMetricId(uuidV7());
    }

    public static AntennaId antennaId() {
        return new AntennaId(uuidV7());
    }

    public static NetworkIndicatorId networkIndicatorId() {
        return new NetworkIndicatorId(uuidV7());
    }

    public static PrivacySummaryId privacySummaryId() {
        return new PrivacySummaryId(uuidV7());
    }

    public static RegionId regionId() {
        return new RegionId(uuidV7());
    }

    public static SocialIndicatorId socialIndicatorId() {
        return new SocialIndicatorId(uuidV7());
    }

    public static DataSourceId dataSourceId() {
        return new DataSourceId(uuidV7());
    }

    public static QueryText queryText() {
        return new QueryText("Comparar conectividad por region");
    }

    public static AnswerSummary answerSummary() {
        return new AnswerSummary("Resumen valido de la respuesta");
    }

    public static AnswerExplanation answerExplanation() {
        return new AnswerExplanation("Explicacion suficientemente extensa para una respuesta valida");
    }

    public static AnswerEvidence answerEvidence() {
        return new AnswerEvidence("Evidencia consolidada valida");
    }

    public static AnswerWarning answerWarning() {
        return new AnswerWarning("Advertencia valida");
    }

    public static MetricRatio metricRatio(String value) {
        return new MetricRatio(decimal(value));
    }

    public static FlowDistanceKm flowDistanceKm(String value) {
        return new FlowDistanceKm(decimal(value));
    }

    public static FlowPercentage flowPercentage(String value) {
        return new FlowPercentage(decimal(value));
    }

    public static Ecgi ecgi(String value) {
        return new Ecgi(value);
    }

    public static Ecgi originEcgi() {
        return ecgi("123456789012");
    }

    public static Ecgi destinationEcgi() {
        return ecgi("123456789013");
    }

    public static GeoPoint geoPoint() {
        return new GeoPoint(decimal("-34.603700"), decimal("-58.381600"));
    }

    public static GeoPoint destinationGeoPoint() {
        return new GeoPoint(decimal("-34.615800"), decimal("-58.433300"));
    }

    public static IndicatorScore indicatorScore(String value) {
        return new IndicatorScore(decimal(value));
    }

    public static Period period() {
        return new Period(PeriodType.TARDE);
    }

    public static SourceFileName sourceFileName() {
        return new SourceFileName("conectividad.csv");
    }

    public static RegionCode regionCode() {
        return new RegionCode("AR-CBA-001");
    }

    public static PrivacyParameter privacyParameter() {
        return new PrivacyParameter("k_anonymity");
    }

    public static PrivacyValue privacyValue() {
        return new PrivacyValue("10");
    }

    public static AiQuery aiQuery() {
        return new AiQuery(
                aiQueryId(),
                queryText(),
                Language.ES,
                AiIntent.CONNECTIVITY_GAP,
                List.of(regionId()),
                period()
        );
    }

    public static AiAnswer aiAnswer() {
        return new AiAnswer(
                aiAnswerId(),
                aiQueryId(),
                answerSummary(),
                answerExplanation(),
                List.of(answerEvidence()),
                List.of(regionId()),
                List.of(dataSourceId()),
                List.of(),
                VisualizationType.MAP,
                ConfidenceLevel.HIGH
        );
    }

    public static ConcentrationMetric concentrationMetric() {
        return new ConcentrationMetric(
                concentrationMetricId(),
                dataSourceId(),
                regionId(),
                originEcgi(),
                "Cluster Centro",
                "Cordoba",
                LocalDate.of(2026, 1, 15),
                period(),
                100L,
                180L,
                1_000L,
                500L,
                120,
                metricRatio("0.1250"),
                metricRatio("0.2500"),
                60,
                25,
                geoPoint()
        );
    }

    public static IngestionRun ingestionRun() {
        return new IngestionRun(
                ingestionRunId(),
                dataSourceId(),
                sourceFileName(),
                IngestionState.RUNNING,
                10L,
                5L,
                2L,
                Instant.parse("2026-01-15T10:00:00Z"),
                null,
                null
        );
    }

    public static MobilityFlow mobilityFlow() {
        return new MobilityFlow(
                mobilityFlowId(),
                dataSourceId(),
                regionId(),
                regionId(),
                originEcgi(),
                destinationEcgi(),
                geoPoint(),
                destinationGeoPoint(),
                "Cluster Norte",
                "Cluster Sur",
                "Cordoba",
                "Villa Maria",
                20L,
                35L,
                flowDistanceKm("12.500"),
                period(),
                flowPercentage("62.500")
        );
    }

    public static OriginDestinationFlow originDestinationFlow() {
        return new OriginDestinationFlow(
                originDestinationFlowId(),
                dataSourceId(),
                regionId(),
                regionId(),
                "Cluster Norte",
                "Cluster Sur",
                "Cordoba",
                "Villa Maria",
                geoPoint(),
                destinationGeoPoint(),
                false,
                20L,
                35L,
                flowDistanceKm("12.500"),
                period()
        );
    }

    public static TravelDistanceMetric travelDistanceMetric() {
        return new TravelDistanceMetric(
                travelDistanceMetricId(),
                dataSourceId(),
                regionId(),
                regionId(),
                "Cluster Norte",
                "Cluster Sur",
                false,
                35L,
                flowDistanceKm("12.500"),
                flowDistanceKm("8.000"),
                flowDistanceKm("18.000"),
                period()
        );
    }

    public static Antenna antenna() {
        return new Antenna(
                antennaId(),
                originEcgi(),
                regionId(),
                "Cluster Centro",
                "Cordoba",
                geoPoint(),
                dataSourceId()
        );
    }

    public static NetworkIndicator networkIndicator() {
        return new NetworkIndicator(
                networkIndicatorId(),
                regionId(),
                dataSourceId(),
                NetworkIndicatorType.CONNECTIVITY_INDEX,
                indicatorScore("0.7500"),
                IndicatorUnit.SCORE,
                GapLevel.MEDIUM,
                ConfidenceLevel.HIGH,
                period(),
                "Descripcion valida del indicador de red"
        );
    }

    public static PrivacySummary privacySummary() {
        return new PrivacySummary(
                privacySummaryId(),
                dataSourceId(),
                privacyParameter(),
                privacyValue()
        );
    }

    public static Region region() {
        return new Region(
                regionId(),
                regionCode(),
                "Cluster Centro",
                "Nueva Cordoba",
                "Cordoba",
                geoPoint()
        );
    }

    public static SocialIndicator socialIndicator() {
        return new SocialIndicator(
                socialIndicatorId(),
                regionId(),
                dataSourceId(),
                SocialIndicatorType.TRAINING,
                indicatorScore("0.6500"),
                IndicatorUnit.SCORE,
                GapLevel.MEDIUM,
                ConfidenceLevel.HIGH,
                "Descripcion valida del indicador social"
        );
    }

    public static DataSource dataSource() {
        return new DataSource(
                dataSourceId(),
                "Fuente sintetica",
                sourceFileName(),
                DataSourceType.SYNTHETIC_DATASET,
                "Descripcion valida de la fuente",
                null, null, null, null
        );
    }
}

