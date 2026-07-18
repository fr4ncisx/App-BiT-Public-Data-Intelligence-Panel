package com.appbit.geoanalytics.application.ai.in;

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
import java.util.regex.Pattern;
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

        var prompt = buildPrompt(evidence, intent, lang);

        try {
            var chatClient = chatClientBuilder.build();
            var content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseResponse(content, evidence, lang);
        } catch (Exception e) {
            log.error("Gemini API call failed — returning fallback response: {}", e.getMessage());
            return fallbackResponse(evidence, lang);
        }
    }

    private String buildPrompt(EvidenceContext evidence, AiIntent intent, Language lang) {
        return switch (lang) {
            case EN -> """
                    You are a territorial analysis assistant. Your role is to answer questions about regional data.
                    
                    RULES:
                    - Do NOT make up data. Use ONLY the provided evidence.
                    - Do not add external information.
                    - If there is insufficient data, clearly state the limitation.
                    
                    DETECTED INTENT: %s
                    
                    AVAILABLE EVIDENCE:
                    Regions: %s
                    Indicators: %s
                    Sources: %s
                    Warnings: %s
                    
                    Answer EXACTLY in this format, with no markdown, no bold, no extra text:
                    
                    SUMMARY: <brief summary 1-2 sentences>
                    EXPLANATION: <detailed explanation based on the data>
                    SUGGESTED_VISUALIZATION: MAP|TABLE|RANKING|FLOW|NONE
                    """.formatted(
                    intent.name(),
                    formatRegions(evidence.regions()),
                    formatIndicators(evidence.indicators()),
                    String.join(", ", evidence.sources()),
                    formatWarnings(evidence.warnings())
            );
            case PT -> """
                    Você é um assistente de análise territorial. Sua função é responder perguntas sobre dados regionais.
                    
                    REGRAS:
                    - NÃO invente dados. Use APENAS as evidências fornecidas.
                    - Não adicione informações externas.
                    - Se não houver dados suficientes, indique claramente a limitação.
                    
                    INTENÇÃO DETECTADA: %s
                    
                    EVIDÊNCIAS DISPONÍVEIS:
                    Regiões: %s
                    Indicadores: %s
                    Fontes: %s
                    Avisos: %s
                    
                    Responda EXATAMENTE neste formato, sem markdown, sem negrito, sem texto extra:
                    
                    SUMMARY: <resumo breve 1-2 frases>
                    EXPLANATION: <explicação detalhada baseada nos dados>
                    SUGGESTED_VISUALIZATION: MAP|TABLE|RANKING|FLOW|NONE
                    """.formatted(
                    intent.name(),
                    formatRegions(evidence.regions()),
                    formatIndicators(evidence.indicators()),
                    String.join(", ", evidence.sources()),
                    formatWarnings(evidence.warnings())
            );
            default -> """
                    Eres un asistente de análisis territorial. Tu función es responder preguntas sobre datos regionales.
                    
                    REGLAS:
                    - NO inventes datos. Usa SOLO la evidencia proporcionada.
                    - No agregues información externa.
                    - Si no hay datos suficientes, indica claramente la limitación.
                    - Presenta números redondeados como enteros o con máximo 1 decimal. Ej: 85%% no 0.8542, 60.4 no 60.428571.
                    - Evita jerga técnica. Habla como un analista que explica a un gerente.
                    - Sé específico pero conciso. Máximo 3 oraciones en EXPLANATION.
                    - Usa lenguaje claro y directo. Evita muletillas como "en este contexto" o "es importante señalar".
                    
                    INTENCIÓN DETECTADA: %s
                    
                    EVIDENCIA DISPONIBLE:
                    Regiones: %s
                    Indicadores: %s
                    Fuentes: %s
                    Advertencias: %s
                    
                    Responde EXACTAMENTE en este formato, sin markdown, sin negritas, sin texto extra:
                    
                    SUMMARY: <resumen breve 1-2 oraciones, claro y directo>
                    EXPLANATION: <explicación detallada basada en los datos, con números redondeados y lenguaje claro>
                    SUGGESTED_VISUALIZATION: MAP|TABLE|RANKING|FLOW|NONE
                    """.formatted(
                        intent.name(),
                        formatRegions(evidence.regions()),
                        formatIndicators(evidence.indicators()),
                        String.join(", ", evidence.sources()),
                        formatWarnings(evidence.warnings())
                );
    };
    }

    private String formatRegions(List<RegionEvidenceDTO> regions) {
        if (regions.isEmpty()) return "Ninguna";
        return String.join("; ", regions.stream()
                .map(r -> r.regionCode() + " - " + r.regionName())
                .toList());
    }

    private String formatIndicators(List<IndicatorEvidenceDTO> indicators) {
        if (indicators.isEmpty()) return "Ninguno";
        return String.join("; ", indicators.stream()
                .map(i -> i.indicatorType() + "=" + i.value() + " " + i.unit())
                .toList());
    }

    private String formatWarnings(List<WarningDTO> warnings) {
        if (warnings.isEmpty()) return "Ninguna";
        var parts = warnings.stream()
                .map(w -> w.type() + ": " + w.message())
                .toList();
        return String.join("; ", parts);
    }

    private AIResponseDTO parseResponse(String content, EvidenceContext evidence, Language lang) {
        var summary = extractField(content, "SUMMARY");
        var explanation = extractField(content, "EXPLANATION");
        var visualization = extractField(content, "SUGGESTED_VISUALIZATION");

        return new AIResponseDTO(
                summary != null ? summary : localizedMessage(lang, "No se pudo generar un resumen.", "Could not generate summary.", "Não foi possível gerar um resumo."),
                explanation != null ? explanation : localizedMessage(lang, "No se pudo generar una explicación.", "Could not generate an explanation.", "Não foi possível gerar uma explicação."),
                evidence.indicators(),
                evidence.regions(),
                evidence.sources(),
                evidence.warnings(),
                visualization != null ? visualization : "NONE"
        );
    }

    private static String extractField(String content, String field) {
        var escaped = Pattern.quote(field);
        var pattern = Pattern.compile(
                "(?:[*_]{1,2})?" + escaped + "\\s*:\\s*(.*?)(?:\\n|$)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        var matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).strip() : null;
    }

    private AIResponseDTO insufficientEvidenceResponse(Language lang) {
        var title = localizedMessage(lang,
                "No hay datos suficientes para responder la consulta.",
                "There is not enough data to answer the query.",
                "Não há dados suficientes para responder à consulta.");
        var detail = localizedMessage(lang,
                "Los filtros seleccionados no coinciden con datos disponibles en la base de datos. Intente con una región, indicador o período diferente.",
                "The selected filters do not match any data in the database. Try a different region, indicator, or period.",
                "Os filtros selecionados não correspondem a dados disponíveis no banco de dados. Tente uma região, indicador ou período diferente.");
        var warning = new WarningDTO("INFO", localizedMessage(lang,
                "No hay datos disponibles para los filtros seleccionados.",
                "No data available for the selected filters.",
                "Não há dados disponíveis para os filtros selecionados."));
        return new AIResponseDTO(title, detail, List.of(), List.of(), List.of(), List.of(warning), "NONE");
    }

    private AIResponseDTO fallbackResponse(EvidenceContext evidence, Language lang) {
        var title = localizedMessage(lang,
                "El servicio de inteligencia artificial no está disponible en este momento.",
                "The artificial intelligence service is currently unavailable.",
                "O serviço de inteligência artificial não está disponível no momento.");
        var detail = localizedMessage(lang,
                "No fue posible generar una respuesta con IA debido a una falla temporal del servicio. Los datos recopilados se muestran a continuación para referencia. Intente nuevamente más tarde.",
                "Could not generate an AI response due to a temporary service failure. The collected data is shown below for reference. Please try again later.",
                "Não foi possível gerar uma resposta com IA devido a uma falha temporária do serviço. Os dados coletados são mostrados abaixo para referência. Tente novamente mais tarde.");
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
                        "El servicio de IA no está disponible. Datos mostrados sin análisis.",
                        "AI service unavailable. Data shown without analysis.",
                        "Serviço de IA indisponível. Dados mostrados sem análise."))),
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
