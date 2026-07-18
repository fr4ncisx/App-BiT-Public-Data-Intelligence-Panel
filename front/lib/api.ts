"use client";

// ─────────────────────────────────────────────────────────────────────────────
// Domain Types — aligned with Spring Boot DTOs
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Indicator types as defined in the backend domain (SocialIndicatorType enum).
 * These are the canonical values accepted by the API.
 */
export type IndicatorType =
  | "TRAINING"
  | "EMPLOYABILITY"
  | "MENTAL_HEALTH"
  | "MENTORSHIP"
  | "SOCIAL_EXPERIENCE";

/**
 * Mapping from backend IndicatorType → user-facing Spanish label.
 * Used throughout the UI to keep labels in Spanish while communicating
 * with the backend using its canonical enum keys.
 */
export const INDICATOR_LABELS: Record<IndicatorType, string> = {
  TRAINING: "Programas de Formación",
  EMPLOYABILITY: "Empleabilidad",
  MENTAL_HEALTH: "Salud Mental",
  MENTORSHIP: "Mentoría",
  SOCIAL_EXPERIENCE: "Experiencia Social",
};

/** All available indicator types in display order */
export const ALL_INDICATORS: IndicatorType[] = [
  "TRAINING",
  "EMPLOYABILITY",
  "MENTAL_HEALTH",
  "MENTORSHIP",
  "SOCIAL_EXPERIENCE",
];

// ── Catalog ────────────────────────────────────────────────────────────────────

/** Aligned with backend `CatalogResponse` */
export interface Catalog {
  /** Geographic regions available in the system */
  regions: RegionSummary[];
  /** Available indicator types (canonical backend values) */
  indicatorTypes: IndicatorType[];
  /** Available time periods */
  periods: string[];
  /** Data sources listed in the catalog */
  sources: SourceSummary[];
}

/** Aligned with backend `RegionSummaryDTO` */
export interface RegionSummary {
  regionCode: string;
  regionName: string;
  municipality: string;
}

/** Aligned with backend `SourceSummaryDTO` */
export interface SourceSummary {
  name: string;
  file: string;
  sourceType: string;
}

// ── Data Sources ──────────────────────────────────────────────────────────────

/** Aligned with backend `SourceDTO` */
export interface DataSource {
  sourceName: string;
  fileName: string;
  sourceType: string;
  description: string;
  confidenceLevel: "HIGH" | "MEDIUM" | "LOW" | null;
  periodStart: string | null;
  periodEnd: string | null;
  governanceType: string | null;
  lastIngestionState: "COMPLETED" | "FAILED" | "IN_PROGRESS" | "PENDING";
  lastIngestionStartedAt: string | null;
  lastIngestionFinishedAt: string | null;
  rowsRead: number;
  rowsInserted: number;
  rowsRejected: number;
  errorMessage: string | null;
}

// ── Region Map ────────────────────────────────────────────────────────────────

/** Aligned with backend `RegionsMapResponse.RegionMapDTO` */
export interface RegionMapDTO {
  id: string;
  regionCode: string;
  regionName: string;
  municipality: string;
  geoPoint: { lat: number; lng: number };
  indicators: RegionIndicatorsDTO | null;
}

/** Aligned with backend `RegionsMapResponse.RegionIndicatorsDTO` */
export interface RegionIndicatorsDTO {
  populationConcentration: IndicatorDetailDTO | null;
  networkCoverage: IndicatorDetailDTO | null;
  trainingPrograms: IndicatorDetailDTO | null;
}

/** Aligned with backend `RegionsMapResponse.IndicatorDetailDTO` */
export interface IndicatorDetailDTO {
  value: number;
  unit: string;
  source: string;
}

/**
 * Aggregated region view — derived from grouping backend sub-regions into
 * the five macro-zones used in the map. Computed in the frontend adapter.
 */
export interface Region {
  id: string;
  name: string;
  regionName?: string;
  type: "CENTRO" | "NORTE" | "SUL" | "LESTE" | "CONTINENTE";
  gapScore: number; // 0–100 (higher = larger gap)
  coordinates: [number, number][]; // center coordinate
  population: number;
  metrics: {
    connectivity: number;     // networkCoverage % (0–100)
    digitalLiteracy: number;  // trainingPrograms % (0–100)
    mentorEngagement: number; // active mentors count
    socialIndex: number;      // populationConcentration score (0–100)
  };
  details: string;
}

// ── AI Query ──────────────────────────────────────────────────────────────────

/** Aligned with backend `AIResponseDTO` */
export interface AiAnswer {
  /** Concise summary of the AI analysis */
  summary: string;
  /** Detailed explanation of the analysis */
  explanation: string;
  /** Evidence indicators used */
  data: IndicatorEvidenceDTO[];
  /** Regions involved in the analysis */
  regions: RegionEvidenceDTO[];
  /** Data sources consulted */
  sources: string[];
  /** Warnings about data quality or limitations */
  warnings: WarningDTO[];
  /**
   * Visualization suggested by the AI for the results.
   * Used to drive the UI response layout.
   */
  suggestedVisualization: "MAP" | "TABLE" | "RANKING" | "FLOW" | "NONE";
}

/** Aligned with backend `IndicatorEvidenceDTO` */
export interface IndicatorEvidenceDTO {
  indicatorType: string;
  regionCode?: string;
  value?: number;
  unit?: string;
}

/** Aligned with backend `RegionEvidenceDTO` */
export interface RegionEvidenceDTO {
  regionCode: string;
  regionName: string;
  municipality: string;
  centerLat: number;
  centerLng: number;
}

/** Aligned with backend `WarningDTO` */
export interface WarningDTO {
  message: string;
  field?: string;
}

// ── Rankings ──────────────────────────────────────────────────────────────────

/** Aligned with backend `RegionRankingResponse` */
export interface RegionRankingResponse {
  items: RankingItem[];
}

/** Aligned with backend `RegionRankingResponse.RankingItem` */
export interface RankingItem {
  regionCode: string;
  regionName: string;
  score: number;
  gapLevel: "HIGH" | "MEDIUM" | "LOW";
}

// ── Social Gap ────────────────────────────────────────────────────────────────

/** Aligned with backend `SocialGapResponse` */
export interface SocialGapResponse {
  indicators: SocialGapItem[];
}

/** Aligned with backend `SocialGapResponse.SocialGapItem` */
export interface SocialGapItem {
  regionCode: string;
  regionName: string;
  municipality: string;
  score: number;
  unit: string;
  gapLevel: "HIGH" | "MEDIUM" | "LOW";
  confidenceLevel: "HIGH" | "MEDIUM" | "LOW";
  description: string;
  sourceType: string;
}

// ── API Fetch Utility ──────────────────────────────────────────────────────────

export interface ApiFetchOptions extends RequestInit {
  forceThrowOnFailure?: boolean;
}

/**
 * Fetches AI-generated suggested questions from the backend.
 * Questions are pre-generated at cold start and refreshed every 10 minutes.
 */
export async function apiFetchSuggestedQuestions(): Promise<string[]> {
  try {
    const res = await apiFetch<string[]>("/data/suggestions");
    return res.data;
  } catch {
    return [
      "¿Qué zona tiene la mayor brecha de conectividad digital?",
      "¿Dónde se necesitan más mentores según la vulnerabilidad social?",
      "Comparar el acceso a banda ancha entre Norte y Continente",
      "¿Cuáles son las zonas con menor nivel de alfabetización digital?",
    ];
  }
}

/** Internal shape of raw backend sub-region objects from `/api/v1/maps/regions` */
interface BackendRegion {
  regionCode?: string;
  regionName?: string;
  municipality?: string;
  geoPoint?: { lat: number; lng: number };
  indicators?: {
    networkCoverage?: { value: number };
    trainingPrograms?: { value: number };
    populationConcentration?: { value: number };
  };
}

export async function apiFetch<T>(path: string, options?: ApiFetchOptions): Promise<{ data: T; error?: string }> {
  let baseUrl = (process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");
  if (!baseUrl.includes("/api/v1")) {
    baseUrl = `${baseUrl}/api/v1`;
  }
  const relativePath = path.startsWith("/") ? path : `/${path}`;
  const url = `${baseUrl}${relativePath}`;

  const { forceThrowOnFailure, ...fetchOptions } = options || {};

  try {
    const response = await fetch(url, {
      ...fetchOptions,
      headers: {
        "Content-Type": "application/json",
        ...(fetchOptions?.headers || {}),
      },
    });

    if (!response.ok) {
      throw new Error(`Error de servidor: ${response.status} ${response.statusText}`);
    }

    const jsonResponse = await response.json() as Record<string, unknown>;

    // Unwrap standard ApiResponse envelope { success, code, message, data, meta }
    if (jsonResponse && "success" in jsonResponse && "data" in jsonResponse) {
      const backendData = jsonResponse.data as Record<string, unknown>;
      const normalizedPath = path.split("?")[0].toLowerCase();

      let mappedData: unknown = backendData;

      if (normalizedPath.endsWith("/data/catalog")) {
        mappedData = mapBackendCatalog(backendData);
      } else if (normalizedPath.endsWith("/maps/regions")) {
        const urlParams = new URLSearchParams(path.includes("?") ? path.substring(path.indexOf("?")) : "");
        const activeIndicator = urlParams.get("indicator") as IndicatorType | null || "TRAINING";
        mappedData = mapBackendRegions(backendData, activeIndicator);
      } else if (normalizedPath.endsWith("/data/queries")) {
        // AI response is already in the correct AIResponseDTO shape — pass through
        mappedData = backendData;
      } else if (normalizedPath.endsWith("/data/suggestions")) {
        // Suggestions are already a string[] — pass through
        mappedData = backendData;
      }

      return { data: mappedData as T };
    }

    return { data: jsonResponse as T };
  } catch (err: unknown) {
    if (forceThrowOnFailure) throw err;

    const safeMessage = sanitizeErrorMessage(err);

    if (process.env.NODE_ENV === "production") {
      throw new Error(safeMessage);
    }

    const mockData = getMockDataForPath(path, fetchOptions?.body);
    if (mockData !== null) {
      return { data: mockData as T };
    }

    throw new Error(safeMessage);
  }
}

/**
 * Converts any error into a user-safe message.
 * Never exposes stack traces, HTTP codes, or backend internals.
 */
function sanitizeErrorMessage(err: unknown): string {
  if (!(err instanceof Error)) {
    return "No se pudo conectar con el servidor. Verifique su conexión.";
  }

  const msg = err.message || "";

  // Network / CORS / DNS failures
  if (msg.includes("Failed to fetch") || msg.includes("NetworkError") || msg.includes("ERR_NETWORK")) {
    return "No se pudo conectar con el servidor. Verifique su conexión.";
  }

  // Timeout
  if (msg.includes("Timeout") || msg.includes("timeout") || msg.includes("AbortError")) {
    return "La conexión tardó demasiado. Intente nuevamente.";
  }

  // HTTP errors — never expose status codes or backend messages
  if (msg.startsWith("Error de servidor:")) {
    return "El servidor no está disponible temporalmente. Intente más tarde.";
  }

  // TypeError from fetch itself
  if (err.name === "TypeError") {
    return "No se pudo conectar con el servidor. Verifique su conexión.";
  }

  // Any other error — generic fallback, never leak the original message
  return "Ocurrió un error inesperado. Intente nuevamente.";
}

// ─────────────────────────────────────────────────────────────────────────────
// Backend → Frontend Data Mapping Adapters
// ─────────────────────────────────────────────────────────────────────────────

function mapBackendCatalog(backendData: Record<string, unknown>): Catalog {
  const rawPeriods = (backendData.periods as string[] | undefined) || [];
  const periods = rawPeriods.length > 0 ? rawPeriods : ["MANHA", "TARDE", "NOITE"];

  const rawIndicatorTypes = (backendData.indicatorTypes as string[] | undefined) || [];
  const indicatorTypes: IndicatorType[] = rawIndicatorTypes.length > 0
    ? rawIndicatorTypes as IndicatorType[]
    : ALL_INDICATORS;

  const rawRegions = (backendData.regions as RegionSummary[] | undefined) || [];
  const rawSources = (backendData.sources as SourceSummary[] | undefined) || [];

  return { regions: rawRegions, indicatorTypes, periods, sources: rawSources };
}

/**
 * Aggregates granular backend sub-regions (e.g. CBD_BEIRAMAR, SC401, LAGOA)
 * into five macro-zones used by the interactive map.
 */
function mapBackendRegions(
  backendData: Record<string, unknown>,
  indicator: IndicatorType
): { regions: Region[] } {
  const backendRegions = (backendData.regions as BackendRegion[] | undefined) || [];

  const zoneDefs = [
    {
      id: "continente", name: "Continente", type: "CONTINENTE" as const,
      coordinates: [[-27.59, -48.59]] as [number, number][],
      details: "El área continental de Florianópolis muestra un crecimiento constante pero persisten desafíos de última milla.",
      match: (code: string) => code.includes("SAO_JOSE") || code.includes("ESTREITO") || code.includes("PALHOCA") || code.includes("BIGUACU"),
    },
    {
      id: "centro", name: "Centro Histórico", type: "CENTRO" as const,
      coordinates: [[-27.595, -48.55]] as [number, number][],
      details: "La zona central concentra la infraestructura tecnológica más robusta.",
      match: (code: string) => code.includes("CENTRO") || code.includes("CORREDOR") || code.includes("HISTORICO") || code.includes("TRINDADE") || code.includes("BEIRAMAR") || code.includes("CBD"),
    },
    {
      id: "norte", name: "Norte de la Isla", type: "NORTE" as const,
      coordinates: [[-27.43, -48.45]] as [number, number][],
      details: "Norte de la Isla tiene una población residencial e industrial dinámica.",
      match: (code: string) => code.includes("INGLESES") || code.includes("JURERE") || code.includes("SC401"),
    },
    {
      id: "leste", name: "Leste (Lagunas)", type: "LESTE" as const,
      coordinates: [[-27.58, -48.44]] as [number, number][],
      details: "Zona costera con fuerte perfil comercial y ambiental local.",
      match: (code: string) => code.includes("LAGOA") || code.includes("BARRA"),
    },
    {
      id: "sul", name: "Sul de la Isla", type: "SUL" as const,
      coordinates: [[-27.75, -48.51]] as [number, number][],
      details: "El Sul de la Isla es el sector con mayor brecha crítica registrada.",
      match: (code: string) => code.includes("CAMPECHE") || code.includes("SUL") || code.includes("SANTO_AMARO") || code.includes("GOV_CELSO_RAMOS"),
    },
  ];

  const regions: Region[] = zoneDefs.map((zone) => {
    const matched = backendRegions.filter((r: BackendRegion) => {
      const code = (r.regionCode || "").toUpperCase();
      return zone.match(code);
    });

    const count = matched.length;
    let sumConnectivity = 0, sumLiteracy = 0, sumMentors = 0, sumSocial = 0, totalPopulation = 0;

    matched.forEach((br: BackendRegion) => {
      totalPopulation += 45000;
      const inds = br.indicators || {};
      sumConnectivity += inds.networkCoverage?.value ?? 60;
      sumLiteracy += inds.trainingPrograms?.value != null ? inds.trainingPrograms.value * 100 : 70;
      sumMentors += inds.trainingPrograms?.value != null ? Math.round(inds.trainingPrograms.value * 50) : 20;
      sumSocial += inds.populationConcentration?.value != null ? inds.populationConcentration.value / 10000 : 65;
    });

    const avg = (sum: number) => count > 0 ? Math.round(sum / count) : 0;
    const avgConnectivity = avg(sumConnectivity) || 65;
    const avgLiteracy = avg(sumLiteracy) || 72;
    const avgMentors = avg(sumMentors) || 25;
    const avgSocial = avg(sumSocial) || 70;

    // Map indicator type to gap score computation
    let gapScore = 30;
    if (indicator === "MENTORSHIP" || indicator === "SOCIAL_EXPERIENCE") {
      gapScore = 100 - (avgMentors * 2);
    } else if (indicator === "TRAINING") {
      gapScore = 100 - avgLiteracy;
    } else if (indicator === "EMPLOYABILITY") {
      gapScore = 100 - avgConnectivity;
    } else if (indicator === "MENTAL_HEALTH") {
      gapScore = 100 - avgSocial;
    }

    return {
      id: zone.id,
      name: zone.name,
      type: zone.type,
      gapScore: Math.max(10, Math.min(95, Math.round(gapScore))),
      coordinates: zone.coordinates,
      population: totalPopulation > 0 ? totalPopulation : 80000,
      metrics: {
        connectivity: avgConnectivity,
        digitalLiteracy: avgLiteracy,
        mentorEngagement: avgMentors,
        socialIndex: avgSocial,
      },
      details: `${zone.details} Consolidado de ${count} sub-sectores activos.`,
    };
  });

  return { regions };
}

// ─────────────────────────────────────────────────────────────────────────────
// Offline Mock Dataset (dev/test only — never served in production)
// Mirrors the exact JSON shape the backend would return.
// ─────────────────────────────────────────────────────────────────────────────

function getMockDataForPath(
  path: string,
  requestBody?: BodyInit | null
): unknown {
  const normalizedPath = path.split("?")[0].toLowerCase();

  // ── GET /api/v1/data/suggestions ─────────────────────────────────────────
  if (normalizedPath.endsWith("/data/suggestions")) {
    return [
      "¿Qué zona tiene la mayor brecha de conectividad digital?",
      "¿Dónde se necesitan más mentores según la vulnerabilidad social?",
      "Comparar el acceso a banda ancha entre Norte y Continente",
      "¿Cuáles son las zonas con menor nivel de alfabetización digital?",
    ];
  }

  // ── GET /api/v1/data/catalog ──────────────────────────────────────────────
  if (normalizedPath.endsWith("/data/catalog")) {
    // Returns CatalogResponse shape
    return {
      regions: [
        { regionCode: "CBD_BEIRAMAR",  regionName: "CBD Beiramar",      municipality: "Florianopolis" },
        { regionCode: "TRINDADE",      regionName: "Trindade",           municipality: "Florianopolis" },
        { regionCode: "SC401",         regionName: "SC-401 Norte",       municipality: "Florianopolis" },
        { regionCode: "INGLESES",      regionName: "Ingleses",           municipality: "Florianopolis" },
        { regionCode: "LAGOA",         regionName: "Lagoa da Conceição", municipality: "Florianopolis" },
        { regionCode: "CAMPECHE",      regionName: "Campeche",           municipality: "Florianopolis" },
        { regionCode: "ESTREITO",      regionName: "Estreito",           municipality: "Florianopolis" },
      ],
      indicatorTypes: ["TRAINING", "EMPLOYABILITY", "MENTAL_HEALTH", "MENTORSHIP", "SOCIAL_EXPERIENCE"],
      periods: ["MANHA", "TARDE", "NOITE"],
      sources: [
        { name: "Concentración Poblacional", file: "tensor_concentracao.csv",  sourceType: "CONCENTRATION" },
        { name: "Cobertura de Red",          file: "network_coverage.csv",      sourceType: "NETWORK" },
        { name: "Programas de Formación",    file: "training_programs.csv",     sourceType: "TRAINING" },
      ],
    };
  }

  // ── GET /api/v1/data/sources ──────────────────────────────────────────────
  if (normalizedPath.endsWith("/data/sources")) {
    // Returns SourcesResponse shape
    return {
      sources: [
        {
          sourceName: "Concentración Poblacional",
          fileName: "tensor_concentracao.csv",
          sourceType: "CONCENTRATION",
          description: "Datos de concentración poblacional por período y región censal",
          confidenceLevel: "HIGH",
          periodStart: "MANHA",
          periodEnd: "NOITE",
          governanceType: "SEED",
          lastIngestionState: "COMPLETED",
          lastIngestionStartedAt: "2026-06-01T10:00:00Z",
          lastIngestionFinishedAt: "2026-06-01T10:05:30Z",
          rowsRead: 15000,
          rowsInserted: 14200,
          rowsRejected: 800,
          errorMessage: null,
        },
        {
          sourceName: "Cobertura de Red Móvil",
          fileName: "network_coverage.csv",
          sourceType: "NETWORK",
          description: "Cobertura de red móvil 4G/5G por celda censal",
          confidenceLevel: "HIGH",
          periodStart: "MANHA",
          periodEnd: "NOITE",
          governanceType: "SEED",
          lastIngestionState: "COMPLETED",
          lastIngestionStartedAt: "2026-06-02T09:00:00Z",
          lastIngestionFinishedAt: "2026-06-02T09:03:10Z",
          rowsRead: 12000,
          rowsInserted: 12000,
          rowsRejected: 0,
          errorMessage: null,
        },
        {
          sourceName: "Programas de Formación Digital",
          fileName: "training_programs.csv",
          sourceType: "TRAINING",
          description: "Inscripciones y asistencia a programas de formación digital territorial",
          confidenceLevel: "MEDIUM",
          periodStart: "MANHA",
          periodEnd: "TARDE",
          governanceType: "SEED",
          lastIngestionState: "COMPLETED",
          lastIngestionStartedAt: "2026-06-03T14:00:00Z",
          lastIngestionFinishedAt: "2026-06-03T14:07:45Z",
          rowsRead: 8500,
          rowsInserted: 8210,
          rowsRejected: 290,
          errorMessage: null,
        },
      ],
    };
  }

  // ── GET /api/v1/maps/regions ──────────────────────────────────────────────
  if (normalizedPath.endsWith("/maps/regions")) {
    const urlParams = new URLSearchParams(path.includes("?") ? path.substring(path.indexOf("?")) : "");
    const indicator = (urlParams.get("indicator") as IndicatorType | null) || "TRAINING";
    const period = urlParams.get("period") || "MANHA";

    const improvementFactor = period === "NOITE" ? 0.85 : period === "TARDE" ? 0.92 : 1.0;

    // Returns RegionsMapResponse shape — granular sub-regions
    const subRegions = [
      { regionCode: "CBD_BEIRAMAR", regionName: "CBD Beiramar",       zone: "centro",     networkCoverage: 88, trainingPrograms: 0.85, populationConcentration: 820000 },
      { regionCode: "TRINDADE",     regionName: "Trindade",           zone: "centro",     networkCoverage: 82, trainingPrograms: 0.79, populationConcentration: 650000 },
      { regionCode: "ESTREITO",     regionName: "Estreito",           zone: "continente", networkCoverage: 60, trainingPrograms: 0.61, populationConcentration: 490000 },
      { regionCode: "SC401",        regionName: "SC-401 Norte",       zone: "norte",      networkCoverage: 52, trainingPrograms: 0.55, populationConcentration: 380000 },
      { regionCode: "INGLESES",     regionName: "Ingleses",           zone: "norte",      networkCoverage: 48, trainingPrograms: 0.50, populationConcentration: 420000 },
      { regionCode: "JURERE",       regionName: "Jurerê",             zone: "norte",      networkCoverage: 55, trainingPrograms: 0.58, populationConcentration: 340000 },
      { regionCode: "LAGOA",        regionName: "Lagoa da Conceição", zone: "leste",      networkCoverage: 68, trainingPrograms: 0.72, populationConcentration: 520000 },
      { regionCode: "BARRA",        regionName: "Barra da Lagoa",     zone: "leste",      networkCoverage: 64, trainingPrograms: 0.67, populationConcentration: 440000 },
      { regionCode: "CAMPECHE",     regionName: "Campeche",           zone: "sul",        networkCoverage: 42, trainingPrograms: 0.42, populationConcentration: 310000 },
      { regionCode: "SANTO_AMARO",  regionName: "Santo Amaro",        zone: "sul",        networkCoverage: 38, trainingPrograms: 0.38, populationConcentration: 280000 },
    ];

    const regions = subRegions.map(({ regionCode, regionName, networkCoverage, trainingPrograms, populationConcentration }) => ({
      regionCode,
      regionName,
      municipality: "Florianopolis",
      geoPoint: { lat: -27.595, lng: -48.548 },
      indicators: {
        networkCoverage:          { value: Math.round(networkCoverage * improvementFactor * 10) / 10,         unit: "PERCENT", source: "network_coverage.csv" },
        trainingPrograms:         { value: Math.round(trainingPrograms * improvementFactor * 100) / 100,       unit: "RATIO",   source: "training_programs.csv" },
        populationConcentration:  { value: Math.round(populationConcentration * improvementFactor),            unit: "SCORE",   source: "tensor_concentracao.csv" },
      },
    }));

    // Map BackendRegion[] → Region[] (same as real backend path)
    const backendData = { regions };
    const mapped = mapBackendRegions(backendData, indicator);

    // Suppress unused 'indicator' warning — used for future per-indicator filtering
    void indicator;
    return mapped;
  }

  // ── POST /api/v1/data/queries ─────────────────────────────────────────────
  if (normalizedPath.endsWith("/data/queries")) {
    let queryText = "";
    try {
      const body = typeof requestBody === "string" ? JSON.parse(requestBody) : requestBody;
      queryText = (body as { query?: string })?.query || "";
    } catch { /* ignore parse errors */ }

    const lowerQuery = queryText.toLowerCase();

    if (lowerQuery.includes("zona") || lowerQuery.includes("brecha") || lowerQuery.includes("conectividad") || lowerQuery.includes("cobertura") || lowerQuery.includes("red")) {
      return {
        summary: "Las zonas Sul de la Isla y Continente presentan las mayores brechas de cobertura de red, superando el 50% de déficit crítico.",
        explanation: "El análisis de la matriz territorial 2026 indica que los sectores CAMPECHE, SANTO_AMARO y ESTREITO muestran la menor densidad de puntos de acceso activos. La brecha se amplía en períodos nocturnos (NOITE) con una caída adicional de hasta 15 puntos porcentuales en cobertura efectiva.",
        data: [
          { indicatorType: "EMPLOYABILITY", regionCode: "CAMPECHE",    value: 42, unit: "PERCENT" },
          { indicatorType: "EMPLOYABILITY", regionCode: "SANTO_AMARO", value: 38, unit: "PERCENT" },
          { indicatorType: "EMPLOYABILITY", regionCode: "ESTREITO",    value: 60, unit: "PERCENT" },
        ],
        regions: [
          { regionCode: "CAMPECHE",    regionName: "Campeche",    municipality: "Florianopolis", centerLat: -27.72, centerLng: -48.51 },
          { regionCode: "ESTREITO",    regionName: "Estreito",    municipality: "Florianopolis", centerLat: -27.59, centerLng: -48.57 },
        ],
        sources: ["network_coverage.csv", "tensor_concentracao.csv"],
        warnings: [],
        suggestedVisualization: "MAP",
      };
    }

    if (lowerQuery.includes("mentor") || lowerQuery.includes("formac") || lowerQuery.includes("alfabetiz") || lowerQuery.includes("training")) {
      return {
        summary: "El Norte de la Isla y el Continente presentan la mayor deficiencia en programas de formación digital activos.",
        explanation: "Los datos de inscripción a programas de formación evidencian una concentración severa de mentores en CBD_BEIRAMAR y TRINDADE, mientras que SC401, INGLESES y ESTREITO muestran tasas de participación por debajo del umbral mínimo recomendado (0.60 de ratio).",
        data: [
          { indicatorType: "TRAINING", regionCode: "SC401",    value: 0.55, unit: "RATIO" },
          { indicatorType: "TRAINING", regionCode: "INGLESES", value: 0.50, unit: "RATIO" },
          { indicatorType: "TRAINING", regionCode: "ESTREITO", value: 0.61, unit: "RATIO" },
        ],
        regions: [
          { regionCode: "SC401",    regionName: "SC-401 Norte", municipality: "Florianopolis", centerLat: -27.45, centerLng: -48.45 },
          { regionCode: "INGLESES", regionName: "Ingleses",     municipality: "Florianopolis", centerLat: -27.46, centerLng: -48.44 },
          { regionCode: "ESTREITO", regionName: "Estreito",     municipality: "Florianopolis", centerLat: -27.59, centerLng: -48.57 },
        ],
        sources: ["training_programs.csv"],
        warnings: [{ message: "Datos de mentoría disponibles solo para período MANHA con cobertura completa." }],
        suggestedVisualization: "MAP",
      };
    }

    return {
      summary: `Análisis territorial completado para: "${queryText}".`,
      explanation: "El Centro Histórico (CBD_BEIRAMAR, TRINDADE) lidera los indicadores de cohesión socio-digital, mientras que el sector Sul (CAMPECHE, SANTO_AMARO) continúa demandando inversión prioritaria en infraestructura de conectividad y formación territorial.",
      data: [
        { indicatorType: "TRAINING",   regionCode: "CAMPECHE",   value: 0.42, unit: "RATIO" },
        { indicatorType: "MENTORSHIP", regionCode: "CAMPECHE",   value: 0.35, unit: "RATIO" },
      ],
      regions: [
        { regionCode: "CAMPECHE",    regionName: "Campeche",    municipality: "Florianopolis", centerLat: -27.72, centerLng: -48.51 },
        { regionCode: "CBD_BEIRAMAR",regionName: "CBD Beiramar",municipality: "Florianopolis", centerLat: -27.60, centerLng: -48.55 },
      ],
      sources: ["tensor_concentracao.csv", "training_programs.csv", "network_coverage.csv"],
      warnings: [],
      suggestedVisualization: "MAP",
    };
  }

  // ── GET /api/v1/ranking ───────────────────────────────────────────────────
  if (normalizedPath.startsWith("/ranking")) {
    return {
      items: [
        { regionCode: "CAMPECHE",    regionName: "Campeche",           score: 0.78, gapLevel: "HIGH"   },
        { regionCode: "SANTO_AMARO", regionName: "Santo Amaro",        score: 0.74, gapLevel: "HIGH"   },
        { regionCode: "SC401",       regionName: "SC-401 Norte",       score: 0.61, gapLevel: "HIGH"   },
        { regionCode: "INGLESES",    regionName: "Ingleses",           score: 0.58, gapLevel: "MEDIUM" },
        { regionCode: "ESTREITO",    regionName: "Estreito",           score: 0.52, gapLevel: "MEDIUM" },
        { regionCode: "LAGOA",       regionName: "Lagoa da Conceição", score: 0.44, gapLevel: "MEDIUM" },
        { regionCode: "BARRA",       regionName: "Barra da Lagoa",     score: 0.40, gapLevel: "MEDIUM" },
        { regionCode: "TRINDADE",    regionName: "Trindade",           score: 0.25, gapLevel: "LOW"    },
        { regionCode: "CBD_BEIRAMAR",regionName: "CBD Beiramar",       score: 0.18, gapLevel: "LOW"    },
      ],
    };
  }

  return null;
}
