package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.application.ai.AIResponse;
import com.appbit.geoanalytics.application.ai.AIResponseDTO;
import com.appbit.geoanalytics.application.ai.EvidenceContext;
import com.appbit.geoanalytics.application.ai.IndicatorEvidenceDTO;
import com.appbit.geoanalytics.application.ai.RegionEvidenceDTO;
import com.appbit.geoanalytics.application.ai.WarningDTO;
import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import com.appbit.geoanalytics.domain.ai.enums.Language;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class GenerateAIAnswerService implements GenerateAIAnswerUseCase {

    private final ChatClient.Builder chatClientBuilder;

    @Override
    public AIResponseDTO execute(EvidenceContext evidence, AiIntent intent, @Nullable Language language) {
        var lang = language != null ? language : Language.ES;

        if (!evidence.isEvidenceSufficient()) {
            log.warn("Insufficient evidence for AI query — returning canned response");
            return insufficientEvidenceResponse(lang);
        }

        var systemPrompt = buildSystemPrompt(lang);
        var userPrompt = buildUserPrompt(evidence, intent);

        try {
            var chatClient = chatClientBuilder.build();
            var aiResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(AIResponse.class, spec -> spec
                            .useProviderStructuredOutput()
                            .validateSchema());

            if (aiResponse == null) {
                log.warn("Gemini returned null — returning fallback response");
                return fallbackResponse(evidence, lang);
            }

            return toDTO(aiResponse, evidence, lang);
        } catch (Exception e) {
            log.error("Gemini API call failed — returning fallback response: {}", e.getMessage());
            return fallbackResponse(evidence, lang);
        }
    }

    private String buildSystemPrompt(Language lang) {
        return switch (lang) {
            case EN -> """
                    You are a territorial data analyst for Florianopolis, Brazil.
                    You answer questions about regional data using ONLY the provided evidence.

                    RULES:
                    - Do NOT fabricate data. Reference only explicit evidence.
                    - Do not add external information not present in the evidence.
                    - If evidence is insufficient, clearly state the limitation.
                    - Round numbers to integers or max 1 decimal (e.g., 85 not 0.8542).
                    - Avoid jargon. Explain like an analyst briefing a city manager.
                    - Maximum 3 sentences in EXPLANATION.
                    - No markdown, no bold, no filler phrases.
                    - Always respond in the SAME language as the user question.

                    OUTPUT FORMAT (respond with EXACTLY these 3 fields):
                    summary: 1-2 sentence summary of findings
                    explanation: Data-driven explanation with rounded numbers, max 3 sentences
                    suggestedVisualization: One of MAP, TABLE, RANKING, FLOW, or NONE
                    """;
            case PT -> """
                    Voce e um analista de dados territoriais para Florianopolis, Brasil.
                    Voce responde perguntas sobre dados regionais usando SOMENTE as evidencias fornecidas.

                    REGRAS:
                    - NAO invente dados. Referencie apenas evidencias explicitas.
                    - NAO adicione informacoes externas nao presentes nas evidencias.
                    - Se as evidencias forem insuficientes, indique claramente a limitacao.
                    - Arredondne numeros para inteiros ou maximo 1 decimal (ex: 85 nao 0.8542).
                    - Evite jargao. Explique como um analista que presenta a um gerente.
                    - Maximo 3 frases em EXPLANATION.
                    - Sem markdown, sem negrito, sem frases de efeito.
                    - Responda SEMPRE no MESMO idioma da pergunta do usuario.

                    FORMATO DE SAIDA (responda com EXATAMENTE estes 3 campos):
                    summary: Resumo de 1-2 frases dos resultados
                    explanation: Explicacao baseada nos dados com numeros arredondados, maximo 3 frases
                    suggestedVisualization: Um de MAP, TABLE, RANKING, FLOW ou NONE
                    """;
            default -> """
                    Eres un analista de datos territoriales para Florianopolis, Brasil.
                    Respondes preguntas sobre datos regionales usando SOLO la evidencia proporcionada.

                    REGLAS:
                    - NO inventes datos. Referencia solo evidencia explicita.
                    - No agregues informacion externa no presente en la evidencia.
                    - Si la evidencia es insuficiente, indica claramente la limitacion.
                    - Redondea numeros a enteros o maximo 1 decimal (ej: 85 no 0.8542).
                    - Evita jerga. Explica como un analista que presenta a un gerente.
                    - Maximo 3 oraciones en EXPLANATION.
                    - Sin markdown, sin negritas, sin frases de relleno.
                    - Responde SIEMPRE en el MISMO idioma de la pregunta del usuario.

                    FORMATO DE SALIDA (responde con EXACTAMENTE estos 3 campos):
                    summary: Resumen de 1-2 oraciones de los hallazgos
                    explanation: Explicacion basada en datos con numeros redondeados, maximo 3 oraciones
                    suggestedVisualization: Uno de MAP, TABLE, RANKING, FLOW o NONE
                    """;
        };
    }

    private String buildUserPrompt(EvidenceContext evidence, AiIntent intent) {
        return """
                DETECTED INTENT: %s

                EVIDENCE:
                Regions: %s
                Indicators: %s
                Sources: %s
                Warnings: %s
                """.formatted(
                intent.name(),
                formatRegions(evidence.regions()),
                formatIndicators(evidence.indicators()),
                String.join(", ", evidence.sources()),
                formatWarnings(evidence.warnings())
        );
    }

    private String formatRegions(List<RegionEvidenceDTO> regions) {
        if (regions.isEmpty()) return "None";
        return String.join("; ", regions.stream()
                .map(r -> r.regionCode() + " - " + r.regionName())
                .toList());
    }

    private String formatIndicators(List<IndicatorEvidenceDTO> indicators) {
        if (indicators.isEmpty()) return "None";
        return String.join("; ", indicators.stream()
                .map(i -> i.indicatorType() + "=" + i.value() + " " + i.unit())
                .toList());
    }

    private String formatWarnings(List<WarningDTO> warnings) {
        if (warnings.isEmpty()) return "None";
        return String.join("; ", warnings.stream()
                .map(w -> w.type() + ": " + w.message())
                .toList());
    }

    private AIResponseDTO toDTO(AIResponse response, EvidenceContext evidence, Language lang) {
        return new AIResponseDTO(
                response.summary() != null ? response.summary() :
                        localizedMessage(lang, "No se pudo generar un resumen.", "Could not generate summary.", "Nao foi possivel gerar um resumo."),
                response.explanation() != null ? response.explanation() :
                        localizedMessage(lang, "No se pudo generar una explicacion.", "Could not generate an explanation.", "Nao foi possivel gerar uma explicacao."),
                evidence.indicators(),
                evidence.regions(),
                evidence.sources(),
                evidence.warnings(),
                response.suggestedVisualization() != null ? response.suggestedVisualization() : "NONE"
        );
    }

    private AIResponseDTO insufficientEvidenceResponse(Language lang) {
        var title = localizedMessage(lang,
                "No hay datos suficientes para responder la consulta.",
                "There is not enough data to answer the query.",
                "Nao ha dados suficientes para responder a consulta.");
        var detail = localizedMessage(lang,
                "Los filtros seleccionados no coinciden con datos disponibles en la base de datos. Intente con una region, indicador o periodo diferente.",
                "The selected filters do not match any data in the database. Try a different region, indicator, or period.",
                "Os filtros selecionados nao correspondem a dados disponiveis no banco de dados. Tente uma regiao, indicador ou periodo diferente.");
        var warning = new WarningDTO("INFO", localizedMessage(lang,
                "No hay datos disponibles para los filtros seleccionados.",
                "No data available for the selected filters.",
                "Nao ha dados disponiveis para os filtros selecionados."));
        return new AIResponseDTO(title, detail, List.of(), List.of(), List.of(), List.of(warning), "NONE");
    }

    private AIResponseDTO fallbackResponse(EvidenceContext evidence, Language lang) {
        var title = localizedMessage(lang,
                "El servicio de inteligencia artificial no esta disponible en este momento.",
                "The artificial intelligence service is currently unavailable.",
                "O servico de inteligencia artificial nao esta disponivel no momento.");
        var detail = localizedMessage(lang,
                "No fue posible generar una respuesta con IA debido a una falla temporal del servicio. Los datos recopilados se muestran a continuacion para referencia. Intente nuevamente mas tarde.",
                "Could not generate an AI response due to a temporary service failure. The collected data is shown below for reference. Please try again later.",
                "Nao foi possivel gerar uma resposta com IA devido a uma falha temporaria do servico. Os dados coletados sao mostrados abaixo para referencia. Tente novamente mais tarde.");
        return new AIResponseDTO(
                title, detail,
                evidence.indicators(),
                evidence.regions(),
                evidence.sources(),
                prependFallbackWarning(evidence.warnings(), lang),
                "NONE"
        );
    }

    private List<WarningDTO> prependFallbackWarning(List<WarningDTO> existing, Language lang) {
        return Stream.concat(
                Stream.of(new WarningDTO("WARNING", localizedMessage(lang,
                        "El servicio de IA no esta disponible. Datos mostrados sin analisis.",
                        "AI service unavailable. Data shown without analysis.",
                        "Servico de IA indisponivel. Dados mostrados sem analise."))),
                existing.stream()
        ).toList();
    }

    private static String localizedMessage(Language lang, String es, String en, String pt) {
        return switch (lang) {
            case EN -> en;
            case PT -> pt;
            default -> es;
        };
    }
}
