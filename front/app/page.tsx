"use client";

import React, { useState, useEffect, useTransition, useMemo } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  apiFetch,
  Region,
  Catalog,
  IndicatorType,
  INDICATOR_LABELS,
} from "../lib/api";
import DashboardHeader from "../components/DashboardHeader";
import InteractiveMap from "../components/InteractiveMap";
import DetailPanel from "../components/DetailPanel";
import dynamic from "next/dynamic";
import IngestionStatus from "../components/IngestionStatus";
import IntelligenceQuery from "../components/IntelligenceQuery";
import {
  Network,
  ShieldCheck,
  RefreshCw,
  Sun,
  Sunset,
  Moon,
} from "lucide-react";

// SSR disabled — Recharts requires browser globals
const IndicatorsChart = dynamic(() => import("../components/IndicatorsChart"), {
  ssr: false,
  loading: () => (
    <div className="bg-[#111318] border border-white/5 rounded-lg p-6 h-[280px] flex items-center justify-center">
      <div className="w-8 h-8 border-4 border-emerald-400/25 border-t-emerald-400 rounded-full animate-spin" />
    </div>
  ),
});

// ── Period helper ─────────────────────────────────────────────────────────────

const PERIOD_META: Record<string, { label: string; range: string; Icon: React.FC<{ className?: string }> }> = {
  MANHA:      { label: "Mañana",    range: "06–12 h",  Icon: Sun    },
  TARDE:      { label: "Tarde",     range: "12–18 h",  Icon: Sunset },
  NOITE:      { label: "Noche",     range: "18–00 h",  Icon: Moon   },
  MADRUGADA:  { label: "Madrugada", range: "00–06 h",  Icon: Moon   },
};

// ── Component ─────────────────────────────────────────────────────────────────

export default function Home() {
  const [activePeriod, setActivePeriod]         = useState("");
  const [activeIndicator, setActiveIndicator]   = useState("");
  const [selectedRegion, setSelectedRegion]     = useState<Region | null>(null);
  const [aiHighlightedRegions, setAiHighlightedRegions] = useState<string[]>([]);
  const [isClient, setIsClient]                 = useState(false);
  const [, startTransition]                     = useTransition();

  // Hydration guard
  useEffect(() => {
    startTransition(() => setIsClient(true));
  }, [startTransition]);

  // 1. Catalog
  const { data: catalog, isLoading: catalogLoading, isError: catalogError } = useQuery({
    queryKey: ["catalog"],
    queryFn: async () => {
      const res = await apiFetch<Catalog>("/data/catalog");
      return res.data;
    },
  });

  const defaultPeriod = useMemo(() => catalog?.periods?.[0] ?? "", [catalog]);
  const defaultIndicator = useMemo(
    () => (catalog?.indicatorTypes?.[0] as IndicatorType | undefined) ?? "",
    [catalog]
  );

  useEffect(() => {
    if (defaultPeriod && !activePeriod) startTransition(() => setActivePeriod(defaultPeriod));
  }, [defaultPeriod, activePeriod, startTransition]);

  useEffect(() => {
    if (defaultIndicator && !activeIndicator) startTransition(() => setActiveIndicator(defaultIndicator));
  }, [defaultIndicator, activeIndicator, startTransition]);

  // 2. Regions
  const { data: regions, isLoading: regionsLoading, isError: regionsError } = useQuery({
    queryKey: ["regions", activeIndicator, activePeriod],
    queryFn: async () => {
      if (!activeIndicator || !activePeriod) return null;
      const res = await apiFetch<{ regions: Region[] }>(
        `/maps/regions?indicator=${encodeURIComponent(activeIndicator)}&period=${encodeURIComponent(activePeriod)}`
      );
      return res.data.regions;
    },
    enabled: !!activeIndicator && !!activePeriod,
  });

  // Auto-select first region
  useEffect(() => {
    if (regions && regions.length > 0 && !selectedRegion) {
      startTransition(() => setSelectedRegion(regions[0]));
    }
  }, [regions, selectedRegion, startTransition]);

  const handleNavigateToRegion = (regionName: string) => {
    if (!regionName || !regions) return;
    const lower = regionName.toLowerCase();
    let id = "";
    if (lower.includes("norte")     || lower.includes("sc401")    || lower.includes("ingleses"))              id = "norte";
    else if (lower.includes("centro") || lower.includes("historico") || lower.includes("beiramar") || lower.includes("trindade")) id = "centro";
    else if (lower.includes("sul")    || lower.includes("campeche") || lower.includes("santo"))               id = "sul";
    else if (lower.includes("leste")  || lower.includes("lagoa")    || lower.includes("barra"))               id = "leste";
    else if (lower.includes("continente") || lower.includes("estreito"))                                      id = "continente";
    const matched = regions.find((r) => r.id === id);
    if (matched) startTransition(() => setSelectedRegion(matched));
  };

  // ── Early returns ──────────────────────────────────────────────────────────

  if (!isClient) return null;

  if (catalogLoading || regionsLoading) {
    return (
      <div className="min-h-screen bg-[#060606] text-white flex flex-col items-center justify-center font-mono gap-4">
        <div className="w-10 h-10 border-t-2 border-emerald-400 rounded-full animate-spin" />
        <span className="text-sm uppercase tracking-widest text-emerald-400/80 animate-pulse font-bold">
          Sincronizando Evidencia Territorial...
        </span>
      </div>
    );
  }

  if (!catalog || !regions || catalogError || regionsError) {
    const isCatalogOnly = catalogError && !regionsError;
    const isRegionsOnly = regionsError && !catalogError;

    const errorTitle = isCatalogOnly
      ? "No se pudieron cargar los datos"
      : isRegionsOnly
        ? "No se pudieron cargar todos los datos"
        : "No se pudieron cargar los datos";

    const errorDescription = isCatalogOnly
      ? "Los datos del territorio no se pudieron obtener del servidor. Verifique su conexión a internet y recargue la página."
      : isRegionsOnly
        ? "Algunos datos del territorio no se pudieron obtener. Verifique su conexión a internet y recargue la página."
        : "Los datos del territorio no se pudieron obtener del servidor. Verifique su conexión a internet y recargue la página.";

    return (
      <div className="min-h-screen bg-[#060606] text-white/90 flex flex-col font-mono p-6 justify-center items-center">
        <div className="max-w-xl w-full bg-[#0b0c10] border border-white/10 rounded-sm p-8 space-y-5 shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-[2px] bg-gradient-to-r from-transparent via-emerald-500/50 to-transparent" />
          <div className="text-rose-400">
            <svg viewBox="0 0 120 120" fill="none" className="w-24 h-24 mx-auto">
              <circle cx="60" cy="60" r="56" stroke="currentColor" strokeOpacity="0.08" strokeWidth="1.5" />
              <rect x="56" y="70" width="8" height="20" rx="2" fill="currentColor" fillOpacity="0.3" />
              <rect x="52" y="88" width="16" height="4" rx="2" fill="currentColor" fillOpacity="0.2" />
              <path d="M40 50 C40 35 80 35 80 50 L72 50 C72 42 48 42 48 50 Z" fill="currentColor" fillOpacity="0.15" stroke="currentColor" strokeOpacity="0.4" strokeWidth="1.5" />
              <path d="M38 38 C38 25 82 25 82 38" stroke="currentColor" strokeOpacity="0.12" strokeWidth="1.5" strokeDasharray="4 4" />
              <path d="M32 30 C32 14 88 14 88 30" stroke="currentColor" strokeOpacity="0.08" strokeWidth="1.5" strokeDasharray="4 4" />
              <line x1="46" y1="20" x2="54" y2="28" stroke="currentColor" strokeOpacity="0.6" strokeWidth="2" strokeLinecap="round" />
              <line x1="54" y1="20" x2="46" y2="28" stroke="currentColor" strokeOpacity="0.6" strokeWidth="2" strokeLinecap="round" />
              <line x1="66" y1="20" x2="74" y2="28" stroke="currentColor" strokeOpacity="0.6" strokeWidth="2" strokeLinecap="round" />
              <line x1="74" y1="20" x2="66" y2="28" stroke="currentColor" strokeOpacity="0.6" strokeWidth="2" strokeLinecap="round" />
            </svg>
          </div>
          <h1 className="text-base font-bold uppercase tracking-[0.2em] text-white font-mono text-center">
            {errorTitle}
          </h1>
          <p className="text-sm text-white/50 font-sans leading-relaxed text-center">
            {errorDescription}
          </p>
          <button
            type="button"
            onClick={() => window.location.reload()}
            className="w-full px-5 py-2.5 bg-white text-black hover:bg-white/90 font-bold text-sm rounded-sm transition-all cursor-pointer flex items-center justify-center gap-2 uppercase tracking-wider font-mono"
          >
            <RefreshCw className="w-4 h-4" />
            Recargar
          </button>
        </div>
      </div>
    );
  }

  const periods        = catalog.periods      || [];
  const indicatorTypes = (catalog.indicatorTypes as IndicatorType[]) || [];
  const currentRegions = regions              || [];

  // ── Main render ────────────────────────────────────────────────────────────

  return (
    <div className="min-h-screen bg-[#060606] text-white/90 flex flex-col font-sans selection:bg-white/20 selection:text-white">
      <DashboardHeader />

      <main className="flex-1 px-4 sm:px-6 py-5 flex flex-col gap-3 lg:gap-5">

        {/* ── Filter Panel ─────────────────────────────────────────────────── */}
        <div className="bg-[#0b0c10] border border-white/10 rounded-sm p-4 shadow-md">
          <div className="flex items-center gap-2 pb-3 mb-4 border-b border-white/5">
            <Network className="w-4 h-4 text-emerald-400 flex-shrink-0" />
            <span className="text-xs font-mono text-emerald-400 uppercase tracking-widest font-bold">
              Filtros de Análisis
            </span>
          </div>

          <div className="flex flex-col gap-4">
            {/* Indicator buttons */}
            <div>
              <span className="text-[11px] font-mono text-white/35 uppercase tracking-widest block mb-2">
                Dimensión de análisis
              </span>
              <div className="flex flex-wrap gap-2">
                {indicatorTypes.map((ind) => (
                  <button
                    type="button"
                    key={ind}
                    onClick={() => setActiveIndicator(ind)}
                    id={`indicator-${ind}`}
                    className={`text-sm px-3.5 py-2 rounded-sm border font-mono transition-all cursor-pointer whitespace-nowrap ${
                      activeIndicator === ind
                        ? "bg-emerald-400 text-black border-emerald-400 font-bold"
                        : "bg-transparent text-white/60 border-white/10 hover:border-white/25 hover:text-white"
                    }`}
                  >
                    {INDICATOR_LABELS[ind] ?? ind}
                  </button>
                ))}
              </div>
            </div>

            {/* Period selector — pill buttons, not a dropdown */}
            {periods.length > 0 && (
              <div>
                <span className="text-[11px] font-mono text-white/35 uppercase tracking-widest block mb-2">
                  Franja horaria
                </span>
                <div className="flex gap-2 flex-wrap">
                  {periods.map((p) => {
                    const meta = PERIOD_META[p];
                    const Icon = meta?.Icon;
                    const isActive = activePeriod === p;
                    return (
                      <button
                        type="button"
                        key={p}
                        onClick={() => setActivePeriod(p)}
                        id={`period-${p}`}
                        className={`flex items-center gap-2 text-sm px-3.5 py-2 rounded-sm border font-mono transition-all cursor-pointer ${
                          isActive
                            ? "bg-white/10 text-white border-white/30 font-semibold"
                            : "bg-transparent text-white/50 border-white/8 hover:border-white/20 hover:text-white/80"
                        }`}
                      >
                        {Icon && <Icon className="w-3.5 h-3.5 flex-shrink-0" />}
                        <span className="flex flex-col items-start leading-none gap-0.5">
                          <span>{meta?.label ?? p}</span>
                          {meta?.range && (
                            <span className={`text-[10px] font-normal ${isActive ? "text-white/60" : "text-white/30"}`}>
                              {meta.range}
                            </span>
                          )}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* ── Quick Stats ───────────────────────────────────────────────────── */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          {[
            { label: "Sectores Analizados",  value: `${catalog.regions?.length ?? 0}`,          badge: "Florianópolis", highlight: false },
            { label: "Dimensiones Activas",   value: `${indicatorTypes.length}`,                badge: "Socio-Digital",  highlight: false },
            { label: "Regiones Destacadas",   value: `${currentRegions.length}`,                badge: "Mapa",           highlight: false },
            { label: "Confianza Estadística", value: "98.2%",                                   badge: "k-Anonimato",    highlight: true  },
          ].map(({ label, value, badge, highlight }) => (
            <div
              key={label}
              className="bg-white/[0.02] border border-white/5 p-3.5 rounded-sm flex items-center justify-between gap-2"
            >
              <div className="min-w-0">
                <span className="text-[10px] text-white/35 font-mono uppercase tracking-widest block truncate">{label}</span>
                <span className={`text-xl font-light tracking-tight mt-1 block ${highlight ? "text-emerald-400" : "text-white"}`}>
                  {value}
                </span>
              </div>
              <span className={`text-[10px] px-2 py-0.5 rounded-sm font-mono uppercase tracking-wider flex-shrink-0 ${
                highlight
                  ? "text-emerald-400/80 bg-emerald-400/5 border border-emerald-400/15"
                  : "text-white/50 bg-white/5 border border-white/8"
              }`}>
                {badge}
              </span>
            </div>
          ))}
        </div>

        {/* ── Primary Grid: Map + Detail + AI ───────────────────────────────── */}
        {/* Mobile: stacked. Tablet (md): Map full width, Detail+AI side-by-side.
            Desktop (xl): Map 4/12 | Detail 3/12 | AI 5/12 (prioritized) */}
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-12 gap-4">

          {/* Map — ocupa todo en mobile/tablet-left, 4 cols en xl */}
          <div className="md:col-span-2 xl:col-span-4 h-[500px] lg:h-[600px] xl:h-full min-h-[300px]">
            <InteractiveMap
              regions={currentRegions}
              selectedRegion={selectedRegion}
              onSelectRegion={(r) => startTransition(() => setSelectedRegion(r))}
              activeIndicator={activeIndicator}
              aiHighlightedRegions={aiHighlightedRegions}
            />
          </div>

          {/* Detail Panel — izquierda en tablet, 3 cols en xl */}
          <div className="md:col-span-1 xl:col-span-3">
            <DetailPanel region={selectedRegion} />
          </div>

          {/* AI Query — derecha en tablet, 5 cols en xl (prioritized) */}
          <div className="md:col-span-1 xl:col-span-5">
            <IntelligenceQuery
              onNavigateToRegion={handleNavigateToRegion}
              onAiAnswer={setAiHighlightedRegions}
            />
          </div>
        </div>

        {/* ── Lower Grid: Chart + Ingestion ─────────────────────────────────── */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <IndicatorsChart
            regions={currentRegions}
            activeIndicator={activeIndicator || "TRAINING"}
            onSelectRegion={(r) => startTransition(() => setSelectedRegion(r))}
            selectedRegion={selectedRegion}
          />
          <IngestionStatus />
        </div>

      </main>

      {/* Footer */}
      <footer className="border-t border-white/8 bg-black/50 px-6 py-3.5 flex flex-col sm:flex-row items-center justify-between text-[10px] text-white/25 font-mono uppercase tracking-widest gap-2">
        <span>© 2026 BiT.INTELLIGENCE · Florianópolis, SC</span>
        <div className="flex items-center gap-4">
          <span className="flex items-center gap-1.5">
            <ShieldCheck className="w-3 h-3 text-emerald-400/60" />
            k-Anonimato · Privacidad Asegurada
          </span>
        </div>
      </footer>
    </div>
  );
}
