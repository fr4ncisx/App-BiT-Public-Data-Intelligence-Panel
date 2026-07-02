package com.appbit.geoanalytics.util;

import org.mockito.Mockito;
import tools.jackson.databind.MappingIterator;

import java.util.List;

public final class MappingIteratorMockUtils {

    private MappingIteratorMockUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    @SuppressWarnings("unchecked")
    public static <T> MappingIterator<T> mockIterator(List<T> items) {
        MappingIterator<T> mockIterator = Mockito.mock(MappingIterator.class);
        var listIterator = items.iterator();

        Mockito.when(mockIterator.hasNext()).thenAnswer(_ -> listIterator.hasNext());
        Mockito.when(mockIterator.next()).thenAnswer(_ -> listIterator.next());

        return mockIterator;
    }
}
