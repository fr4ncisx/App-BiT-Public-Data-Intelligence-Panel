package com.appbit.geoanalytics.application.ai.in;

import com.appbit.geoanalytics.domain.ai.enums.AiIntent;
import lombok.RequiredArgsConstructor;

import static com.appbit.geoanalytics.domain.ai.enums.AiIntent.*;

@RequiredArgsConstructor
public class IntentClassifierService implements ClassifyIntentUseCase {

    @Override
    public AiIntent execute(String query) {
        if (query == null || query.isBlank()) {
            return UNKNOWN;
        }

        String lower = query.toLowerCase();

        if (containsAny(lower, "formação", "formación", "capacitación", "capacitação",
                "educação", "educación", "treinamento", "entrenamiento")) return TRAINING_GAP;

        if (containsAny(lower, "emprego", "empleo", "empleabilidad", "empregabilidade",
                "trabalho", "trabajo", "desemprego", "desempleo")) return EMPLOYABILITY_GAP;

        if (containsAny(lower, "saúde mental", "salud mental", "bem-estar", "bienestar",
                "psicológico", "psicológico")) return MENTAL_HEALTH_ACCESS;

        if (containsAny(lower, "mentoría", "mentoria", "mentorship",
                "orientação", "orientación")) return MENTORSHIP_NEED;

        if (containsAny(lower, "experiência social", "experiencia social",
                "socialização", "socialización", "convivência")) return SOCIAL_EXPERIENCE;

        if (containsAny(lower, "conectividade", "conectividad", "internet",
                "4g", "5g", "banda larga", "banda ancha")) return CONNECTIVITY_GAP;

        if (containsAny(lower, "concentração", "concentración", "população", "población",
                "habitantes", "densidade", "densidad")) return POPULATION_CONCENTRATION;

        if (containsAny(lower, "comparar", "comparação", "comparación",
                "vs", "versus", "diferença", "diferencia")) return REGION_COMPARISON;

        if (containsAny(lower, "fonte", "fuente", "origem", "origen",
                "dados", "dados", "procedência")) return SOURCE_EXPLANATION;

        return UNKNOWN;
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
