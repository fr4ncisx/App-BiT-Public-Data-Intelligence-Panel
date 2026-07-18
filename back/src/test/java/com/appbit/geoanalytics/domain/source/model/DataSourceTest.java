package com.appbit.geoanalytics.domain.source.model;

import com.appbit.geoanalytics.domain.exception.IdentityRestrictionException;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.exception.SourceDomainException;
import com.appbit.geoanalytics.domain.source.vo.DataSourceId;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.appbit.geoanalytics.domain.testing.DomainFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSourceTest {

    @Test
    void shouldCreateValidDataSourceAndTrimTexts() {
        DataSource source = new DataSource(
                dataSourceId(),
                "  Fuente sintetica  ",
                new SourceFileName("  conectividad.CSV  "),
                DataSourceType.SYNTHETIC_DATASET,
                "  Descripcion valida de la fuente  ",
                null, null, null, null
        );

        assertThat(source.getSourceName()).isEqualTo("Fuente sintetica");
        assertThat(source.getFileName().value()).isEqualTo("conectividad.CSV");
        assertThat(source.getDescription()).isEqualTo("Descripcion valida de la fuente");
    }

    @Test
    void shouldRejectNullRequiredFields() {
        assertThatThrownBy(() -> new DataSource(null, "Fuente", sourceFileName(), DataSourceType.SYNTHETIC_DATASET, "Descripcion valida", null, null, null, null))
                .isInstanceOf(SourceDomainException.class).hasMessage("Data source id cannot be null");
        assertThatThrownBy(() -> new DataSource(dataSourceId(), "Fuente", null, DataSourceType.SYNTHETIC_DATASET, "Descripcion valida", null, null, null, null))
                .isInstanceOf(SourceDomainException.class).hasMessage("Source file name cannot be null");
        assertThatThrownBy(() -> new DataSource(dataSourceId(), "Fuente", sourceFileName(), null, "Descripcion valida", null, null, null, null))
                .isInstanceOf(SourceDomainException.class).hasMessage("Data source type cannot be null");
    }

    @Test
    void shouldRejectInvalidTexts() {
        assertThatThrownBy(() -> sourceWithTexts(null, "Descripcion valida")).isInstanceOf(SourceDomainException.class).hasMessage("Source name cannot be null");
        assertThatThrownBy(() -> sourceWithTexts(" ", "Descripcion valida")).isInstanceOf(SourceDomainException.class).hasMessage("Source name cannot be blank");
        assertThatThrownBy(() -> sourceWithTexts("A", "Descripcion valida")).isInstanceOf(SourceDomainException.class).hasMessage("Source name length must be between 2 and 120 characters");
        assertThatThrownBy(() -> sourceWithTexts(text(121), "Descripcion valida")).isInstanceOf(SourceDomainException.class).hasMessage("Source name length must be between 2 and 120 characters");
        assertThatThrownBy(() -> sourceWithTexts("Fuente\nSintetica", "Descripcion valida")).isInstanceOf(SourceDomainException.class).hasMessage("Source name cannot contain control characters");

        assertThatThrownBy(() -> sourceWithTexts("Fuente", null)).isInstanceOf(SourceDomainException.class).hasMessage("Description cannot be null");
        assertThatThrownBy(() -> sourceWithTexts("Fuente", " ")).isInstanceOf(SourceDomainException.class).hasMessage("Description cannot be blank");
        assertThatThrownBy(() -> sourceWithTexts("Fuente", "abcd")).isInstanceOf(SourceDomainException.class).hasMessage("Description length must be between 5 and 500 characters");
        assertThatThrownBy(() -> sourceWithTexts("Fuente", text(501))).isInstanceOf(SourceDomainException.class).hasMessage("Description length must be between 5 and 500 characters");
        assertThatThrownBy(() -> sourceWithTexts("Fuente", "Descripcion\ninvalida")).isInstanceOf(SourceDomainException.class).hasMessage("Description cannot contain control characters");
    }

    @Test
    void shouldCompareDataSourcesByIdentity() {
        DataSourceId sameId = dataSourceId();
        DataSource first = new DataSource(sameId, "Fuente sintetica", sourceFileName(), DataSourceType.SYNTHETIC_DATASET, "Descripcion valida de la fuente", null, null, null, null);
        DataSource second = new DataSource(sameId, "Otra fuente", new SourceFileName("otra.csv"), DataSourceType.PUBLIC_SOURCE, "Otra descripcion valida", null, null, null, null);

        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first).isNotEqualTo(dataSource());
        assertThat(first).isNotEqualTo(null);
        assertThat(first).isNotEqualTo("not a source");
    }

    private DataSource sourceWithTexts(String sourceName, String description) {
        return new DataSource(dataSourceId(), sourceName, sourceFileName(), DataSourceType.SYNTHETIC_DATASET, description, null, null, null, null);
    }

    @Nested
    class DataSourceIdTest {
        @Test
        void shouldAcceptUuidV7() {
            DataSourceId id = new DataSourceId(uuidV7());
            assertThat(id.value().version()).isEqualTo(7);
            assertThat(id.value().variant()).isEqualTo(2);
        }

        @Test
        void shouldRejectInvalidUuid() {
            assertThatThrownBy(() -> new DataSourceId(null)).isInstanceOf(IdentityRestrictionException.class).hasMessage("DataSourceId cannot be null.");
            assertThatThrownBy(() -> new DataSourceId(nilUuid())).isInstanceOf(IdentityRestrictionException.class).hasMessage("DataSourceId cannot be the zero UUID: 00000000-0000-0000-0000-000000000000.");
            assertThatThrownBy(() -> new DataSourceId(uuidV4())).isInstanceOf(IdentityRestrictionException.class).hasMessage("DataSourceId must be UUIDv7. Received value: version 4.");
            assertThatThrownBy(() -> new DataSourceId(nonRfc4122UuidV7())).isInstanceOf(IdentityRestrictionException.class).hasMessage("DataSourceId must use RFC 4122/IETF variant. Received value: variant 0.");
        }
    }

    @Nested
    class SourceFileNameTest {
        @Test
        void shouldTrimValidCsvFileName() {
            assertThat(new SourceFileName("  conectividad.CSV  ").value()).isEqualTo("conectividad.CSV");
        }

        @Test
        void shouldRejectInvalidFileName() {
            assertThatThrownBy(() -> new SourceFileName(null)).isInstanceOf(SourceDomainException.class).hasMessage("Source file name cannot be null");
            assertThatThrownBy(() -> new SourceFileName("   ")).isInstanceOf(SourceDomainException.class).hasMessage("Source file name cannot be blank");
            assertThatThrownBy(() -> new SourceFileName("a.cs")).isInstanceOf(SourceDomainException.class).hasMessage("Source file name length must be between 5 and 120 characters");
            assertThatThrownBy(() -> new SourceFileName(text(117) + ".csv")).isInstanceOf(SourceDomainException.class).hasMessage("Source file name length must be between 5 and 120 characters");
            assertThatThrownBy(() -> new SourceFileName("../datos.csv")).isInstanceOf(SourceDomainException.class).hasMessage("Source file name cannot contain path traversal");
            assertThatThrownBy(() -> new SourceFileName("dir/datos.csv")).isInstanceOf(SourceDomainException.class).hasMessage("Source file name cannot contain path separators");
            assertThatThrownBy(() -> new SourceFileName("dir\\datos.csv")).isInstanceOf(SourceDomainException.class).hasMessage("Source file name cannot contain path separators");
            assertThatThrownBy(() -> new SourceFileName("datos\n.csv")).isInstanceOf(SourceDomainException.class).hasMessage("Source file name cannot contain control characters");
            assertThatThrownBy(() -> new SourceFileName("datos.json")).isInstanceOf(SourceDomainException.class).hasMessage("Source file name must be a CSV file");
        }
    }
}
