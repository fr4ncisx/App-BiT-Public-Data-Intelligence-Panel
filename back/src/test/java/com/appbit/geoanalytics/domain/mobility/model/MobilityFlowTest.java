package com.appbit.geoanalytics.domain.mobility.model;

import com.appbit.geoanalytics.domain.mobility.exception.MobilityDomainException;
import com.appbit.geoanalytics.domain.mobility.vo.FlowPercentage;
import com.appbit.geoanalytics.domain.mobility.vo.MobilityFlowId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.appbit.geoanalytics.domain.testing.DomainFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MobilityFlowTest {

    @Test
    void shouldCreateValidFlowAndTrimTexts() {
        MobilityFlow flow = new MobilityFlow(
                mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(),
                geoPoint(), destinationGeoPoint(), "  Cluster Norte  ", "  Cluster Sur  ",
                "  Cordoba  ", "  Villa Maria  ", 20L, 35L, flowDistanceKm("12.500"), period(), flowPercentage("62.500")
        );

        assertThat(flow.getOriginClusterName()).isEqualTo("Cluster Norte");
        assertThat(flow.getDestinationClusterName()).isEqualTo("Cluster Sur");
        assertThat(flow.getOriginMunicipality()).isEqualTo("Cordoba");
        assertThat(flow.getDestinationMunicipality()).isEqualTo("Villa Maria");
    }

    @Test
    void shouldRejectNullRequiredFields() {
        assertThatThrownBy(() -> flowWith(null, dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Mobility flow id cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), null, regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Data source id cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), null, regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Origin region id cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), regionId(), null, originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Destination region id cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), regionId(), regionId(), null, destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Origin ECGI cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), null, geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Destination ECGI cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), null, destinationGeoPoint(), flowDistanceKm("1.000"), period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Origin point cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), null, flowDistanceKm("1.000"), period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Destination point cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), null, period(), flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Distance km cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), null, flowPercentage("50.000")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Predominant period cannot be null");
        assertThatThrownBy(() -> flowWith(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), period(), null))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Origin cluster percentage cannot be null");
    }

    @Test
    void shouldRejectInvalidFlowInvariants() {
        assertThatThrownBy(() -> new MobilityFlow(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), originEcgi(), geoPoint(), destinationGeoPoint(), "Cluster Norte", "Cluster Sur", "Cordoba", "Villa Maria", 20L, 35L, flowDistanceKm("12.500"), period(), flowPercentage("62.500")))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Origin ECGI and destination ECGI cannot be the same");
        assertThatThrownBy(() -> flowWithCounters(-1L, 35L))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Users cannot be negative");
        assertThatThrownBy(() -> flowWithCounters(20L, -1L))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Transitions cannot be negative");
        assertThatThrownBy(() -> flowWithCounters(36L, 35L))
                .isInstanceOf(MobilityDomainException.class).hasMessage("Users cannot be greater than transitions");
    }

    @Test
    void shouldRejectInvalidTexts() {
        assertThatThrownBy(() -> flowWithTexts(null, "Cluster Sur", "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin cluster name cannot be null");
        assertThatThrownBy(() -> flowWithTexts(" ", "Cluster Sur", "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin cluster name cannot be blank");
        assertThatThrownBy(() -> flowWithTexts("A", "Cluster Sur", "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin cluster name length must be between 2 and 40 characters");
        assertThatThrownBy(() -> flowWithTexts(text(41), "Cluster Sur", "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin cluster name length must be between 2 and 40 characters");
        assertThatThrownBy(() -> flowWithTexts("Cluster\nNorte", "Cluster Sur", "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin cluster name cannot contain control characters");

        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", null, "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Destination cluster name cannot be null");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", " ", "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Destination cluster name cannot be blank");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "A", "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Destination cluster name length must be between 2 and 40 characters");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", text(41), "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Destination cluster name length must be between 2 and 40 characters");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster\nSur", "Cordoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Destination cluster name cannot contain control characters");

        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", null, "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin municipality cannot be null");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", " ", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin municipality cannot be blank");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", "A", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin municipality length must be between 2 and 60 characters");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", text(61), "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin municipality length must be between 2 and 60 characters");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", "Cor\ndoba", "Villa Maria")).isInstanceOf(MobilityDomainException.class).hasMessage("Origin municipality cannot contain control characters");

        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", "Cordoba", null)).isInstanceOf(MobilityDomainException.class).hasMessage("Destination municipality cannot be null");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", "Cordoba", " ")).isInstanceOf(MobilityDomainException.class).hasMessage("Destination municipality cannot be blank");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", "Cordoba", "A")).isInstanceOf(MobilityDomainException.class).hasMessage("Destination municipality length must be between 2 and 60 characters");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", "Cordoba", text(61))).isInstanceOf(MobilityDomainException.class).hasMessage("Destination municipality length must be between 2 and 60 characters");
        assertThatThrownBy(() -> flowWithTexts("Cluster Norte", "Cluster Sur", "Cordoba", "Villa\nMaria")).isInstanceOf(MobilityDomainException.class).hasMessage("Destination municipality cannot contain control characters");
    }

    @Test
    void shouldCompareFlowsByIdentity() {
        MobilityFlowId sameId = mobilityFlowId();
        MobilityFlow first = flowWith(sameId, dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("1.000"), period(), flowPercentage("50.000"));
        MobilityFlow second = flowWith(sameId, dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), flowDistanceKm("2.000"), period(), flowPercentage("75.000"));

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(mobilityFlow());
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("not a flow");
    }

    private MobilityFlow flowWith(
            MobilityFlowId id,
            com.appbit.geoanalytics.domain.source.vo.DataSourceId sourceId,
            com.appbit.geoanalytics.domain.region.vo.RegionId originRegionId,
            com.appbit.geoanalytics.domain.region.vo.RegionId destinationRegionId,
            com.appbit.geoanalytics.domain.shared.vo.Ecgi originEcgi,
            com.appbit.geoanalytics.domain.shared.vo.Ecgi destinationEcgi,
            com.appbit.geoanalytics.domain.shared.vo.GeoPoint originPoint,
            com.appbit.geoanalytics.domain.shared.vo.GeoPoint destinationPoint,
            com.appbit.geoanalytics.domain.mobility.vo.FlowDistanceKm distanceKm,
            com.appbit.geoanalytics.domain.shared.vo.Period predominantPeriod,
            FlowPercentage percentage
    ) {
        return new MobilityFlow(id, sourceId, originRegionId, destinationRegionId, originEcgi, destinationEcgi, originPoint, destinationPoint, "Cluster Norte", "Cluster Sur", "Cordoba", "Villa Maria", 20L, 35L, distanceKm, predominantPeriod, percentage);
    }

    private MobilityFlow flowWithCounters(long users, long transitions) {
        return new MobilityFlow(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), "Cluster Norte", "Cluster Sur", "Cordoba", "Villa Maria", users, transitions, flowDistanceKm("12.500"), period(), flowPercentage("62.500"));
    }

    private MobilityFlow flowWithTexts(String originClusterName, String destinationClusterName, String originMunicipality, String destinationMunicipality) {
        return new MobilityFlow(mobilityFlowId(), dataSourceId(), regionId(), regionId(), originEcgi(), destinationEcgi(), geoPoint(), destinationGeoPoint(), originClusterName, destinationClusterName, originMunicipality, destinationMunicipality, 20L, 35L, flowDistanceKm("12.500"), period(), flowPercentage("62.500"));
    }

    @Nested
    class MobilityFlowIdTest {

        @Test
        void shouldAcceptUuidV7() {
            MobilityFlowId id = new MobilityFlowId(uuidV7());

            assertThat(id.value().version()).isEqualTo(7);
            assertThat(id.value().variant()).isEqualTo(2);
        }

        @Test
        void shouldRejectInvalidUuid() {
            assertThatThrownBy(() -> new MobilityFlowId(null)).isInstanceOf(MobilityDomainException.class).hasMessage("Mobility flow id cannot be null");
            assertThatThrownBy(() -> new MobilityFlowId(nilUuid())).isInstanceOf(MobilityDomainException.class).hasMessage("Mobility flow id cannot be nil UUID");
            assertThatThrownBy(() -> new MobilityFlowId(uuidV4())).isInstanceOf(MobilityDomainException.class).hasMessage("Mobility flow id must be UUIDv7");
            assertThatThrownBy(() -> new MobilityFlowId(nonRfc4122UuidV7())).isInstanceOf(MobilityDomainException.class).hasMessage("Mobility flow id must be RFC 4122 compatible");
        }
    }

    @Nested
    class FlowPercentageTest {

        @Test
        void shouldAcceptBoundaries() {
            assertThat(new FlowPercentage(decimal("0.000")).value()).isEqualByComparingTo("0");
            assertThat(new FlowPercentage(decimal("100.000")).value()).isEqualByComparingTo("100");
        }

        @Test
        void shouldRejectInvalidValue() {
            assertThatThrownBy(() -> new FlowPercentage(null)).isInstanceOf(MobilityDomainException.class).hasMessage("Flow percentage cannot be null");
            assertThatThrownBy(() -> new FlowPercentage(decimal("-0.001"))).isInstanceOf(MobilityDomainException.class).hasMessage("Flow percentage must be between 0 and 100");
            assertThatThrownBy(() -> new FlowPercentage(decimal("100.001"))).isInstanceOf(MobilityDomainException.class).hasMessage("Flow percentage must be between 0 and 100");
            assertThatThrownBy(() -> new FlowPercentage(decimal("10.1234"))).isInstanceOf(MobilityDomainException.class).hasMessage("Flow percentage scale cannot exceed 3 decimal places");
        }
    }
}
