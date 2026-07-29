package com.appbit.geoanalytics.infrastructure.adapter.in.rest.queries;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ConditionalOnProperty(name = "spring.ai.chat.client.enabled", havingValue = "true")
public class SuggestedQuestionsService {

    private static final List<String> FALLBACK_QUESTIONS = List.of(
            "¿Qué zona tiene la mayor brecha de conectividad digital?",
            "¿Dónde se necesitan más mentores según la vulnerabilidad social?",
            "Comparar el acceso a banda ancha entre Norte y Continente",
            "¿Cuáles son las zonas con menor nivel de alfabetización digital?"
    );

    private final ChatClient.Builder chatClientBuilder;
    private final Map<String, List<String>> cache = new ConcurrentHashMap<>();

    public SuggestedQuestionsService(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onColdStart() {
        log.info("[SuggestedQuestions] Cold start: generating suggested questions...");
        regenerate();
    }

    @Scheduled(fixedRate = 600000)
    public void onScheduledRefresh() {
        log.info("[SuggestedQuestions] Scheduled refresh: regenerating suggested questions...");
        regenerate();
    }

    public List<String> getSuggestions() {
        return cache.getOrDefault("default", FALLBACK_QUESTIONS);
    }

    private void regenerate() {
        try {
            var chatClient = chatClientBuilder.build();
            var content = chatClient.prompt()
                    .system("""
                            Eres un generador de preguntas para un panel de inteligencia territorial.
                            Solo genera las 4 preguntas. No expliques nada. No uses markdown.
                            Responde SOLO con el formato exacto solicitado.
                            """)
                    .user("""
                            Genera 4 preguntas sugeridas en español para un panel de inteligencia territorial sobre Florianópolis, Brasil.

                            El panel tiene estos datos:
                            - Regiones: 27 sectores de Florianópolis (centro, norte, sul, leste, continente)
                            - Indicadores: formación, empleabilidad, salud mental, mentoría, experiencia social
                            - Datos de conectividad, cobertura de red, concentración poblacional
                            - Periodos: mañana, tarde, noche, madrugada

                            Responde EXACTAMENTE en este formato, sin markdown, sin negritas, sin texto extra:
                            PREGUNTA_1: [pregunta aquí]
                            PREGUNTA_2: [pregunta aquí]
                            PREGUNTA_3: [pregunta aquí]
                            PREGUNTA_4: [pregunta aquí]
                            """)
                    .call()
                    .content();

            var questions = parseQuestions(content);
            if (questions.size() == 4) {
                cache.put("default", questions);
                log.info("[SuggestedQuestions] Successfully generated 4 questions");
            } else {
                log.warn("[SuggestedQuestions] Got {} questions, expected 4. Using fallback.", questions.size());
                cache.put("default", FALLBACK_QUESTIONS);
            }
        } catch (Exception e) {
            log.warn("[SuggestedQuestions] Failed to generate questions: {}. Using fallback.", e.getMessage());
            cache.put("default", FALLBACK_QUESTIONS);
        }
    }

    private List<String> parseQuestions(String content) {
        var questions = new java.util.ArrayList<String>();
        for (var line : content.split("\n")) {
            var trimmed = line.trim();
            if (trimmed.startsWith("PREGUNTA_") && trimmed.contains(":")) {
                var question = trimmed.substring(trimmed.indexOf(":") + 1).trim();
                if (!question.isEmpty()) {
                    questions.add(question);
                }
            }
        }
        return questions;
    }
}
