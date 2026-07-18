package com.appbit.geoanalytics.application.source.in;

import com.appbit.geoanalytics.application.source.out.DataSourcePort;
import com.appbit.geoanalytics.application.source.out.SourceCatalogEntry;
import com.appbit.geoanalytics.domain.source.enums.DataSourceType;
import com.appbit.geoanalytics.domain.source.vo.SourceFileName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static com.appbit.geoanalytics.domain.testing.DomainFixtures.sourceFileName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindDataSourceByFileNameServiceTest {

    private final DataSourcePort dataSourcePort = mock(DataSourcePort.class);
    private final FindDataSourceByFileNameService useCase = new FindDataSourceByFileNameService(dataSourcePort);

    @Test
    void shouldReturnCatalogEntryWhenFileExists() {
        SourceFileName fileName = sourceFileName();
        SourceCatalogEntry expected = new SourceCatalogEntry(
                UUID.randomUUID(),
                "V�sent CDRView",
                fileName,
                DataSourceType.SYNTHETIC_DATASET,
                "Descripci�n v�lida de la fuente",
                null, null, null, null
        );

        when(dataSourcePort.findByFileName(fileName)).thenReturn(Optional.of(expected));

        SourceCatalogEntry result = useCase.execute(fileName);

        assertThat(result).isEqualTo(expected);
        verify(dataSourcePort).findByFileName(fileName);
    }

    @Test
    void shouldThrowExceptionWhenFileNotFound() {
        SourceFileName fileName = new SourceFileName("unknown.csv");

        when(dataSourcePort.findByFileName(fileName)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(fileName))
                .isInstanceOf(DataSourceNotFoundException.class)
                .hasMessage("unknown.csv");
    }
}
