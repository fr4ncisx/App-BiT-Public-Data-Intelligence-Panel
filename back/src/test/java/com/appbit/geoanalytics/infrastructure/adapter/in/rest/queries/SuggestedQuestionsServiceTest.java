package com.appbit.geoanalytics.infrastructure.adapter.in.rest.queries;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.chat.client.ChatClient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SuggestedQuestionsServiceTest {

    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;

    private SuggestedQuestionsService service;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.system(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(String.class))).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        service = new SuggestedQuestionsService(chatClientBuilder);
    }

    @Test
    void returnsFallbackQuestionsWhenCacheIsEmpty() {
        var suggestions = service.getSuggestions();

        assertThat(suggestions).hasSize(4)
                .contains("¿Qué zona tiene la mayor brecha de conectividad digital?");
    }

    @Test
    void coldStartStoresGeneratedQuestionsWhenExactlyFour() {
        when(callResponseSpec.content()).thenReturn(
                "PREGUNTA_1: ¿Cuál es la zona con mayor concentración?\n" +
                        "PREGUNTA_2: ¿Dónde hay menor alfabetización digital?\n" +
                        "PREGUNTA_3: ¿Qué zona necesita más mentores?\n" +
                        "PREGUNTA_4: Comparar acceso a banda ancha entre regiones"
        );

        service.onColdStart();

        assertThat(service.getSuggestions())
                .containsExactly(
                        "¿Cuál es la zona con mayor concentración?",
                        "¿Dónde hay menor alfabetización digital?",
                        "¿Qué zona necesita más mentores?",
                        "Comparar acceso a banda ancha entre regiones"
                );
        verify(chatClientRequestSpec).system(any(String.class));
        verify(chatClientRequestSpec).user(contains("Florianópolis"));
    }

    @Test
    void usesFallbackWhenLessThanFourQuestionsParsed() {
        when(callResponseSpec.content()).thenReturn(
                "PREGUNTA_1: pregunta uno\nPREGUNTA_2: pregunta dos"
        );

        service.onScheduledRefresh();

        assertThat(service.getSuggestions()).hasSize(4)
                .contains("¿Qué zona tiene la mayor brecha de conectividad digital?");
    }

    @Test
    void parsesOnlyMatchingLinesAndSkipsEmptyQuestions() {
        when(callResponseSpec.content()).thenReturn(
                "texto suelto sin formato\n" +
                        "PREGUNTA_1: pregunta uno\n" +
                        "PREGUNTA_2:  \n" +
                        "PREGUNTA_3: pregunta tres\n" +
                        "PREGUNTA_4: pregunta cuatro"
        );

        service.onColdStart();

        assertThat(service.getSuggestions()).hasSize(4)
                .contains("¿Qué zona tiene la mayor brecha de conectividad digital?");
    }

    @Test
    void usesFallbackWhenChatClientThrows() {
        when(chatClientRequestSpec.call()).thenThrow(new RuntimeException("API timeout"));

        service.onColdStart();

        assertThat(service.getSuggestions()).hasSize(4)
                .contains("¿Qué zona tiene la mayor brecha de conectividad digital?");
    }

    @Test
    void scheduledRefreshRegeneratesQuestions() {
        when(callResponseSpec.content()).thenReturn(
                "PREGUNTA_1: nueva uno\nPREGUNTA_2: nueva dos\nPREGUNTA_3: nueva tres\nPREGUNTA_4: nueva cuatro"
        );

        service.onScheduledRefresh();

        assertThat(service.getSuggestions()).hasSize(4)
                .contains("nueva uno");
    }
}
