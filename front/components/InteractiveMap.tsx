"use client";

import React from "react";
import dynamic from "next/dynamic";
import { Region, INDICATOR_LABELS, IndicatorType } from "../lib/api";
import { Map as MapIcon, Sparkles } from "lucide-react";

// SSR disabled — Leaflet requires browser globals
const MapComponent = dynamic(() => import("./MapComponent"), {
  ssr: false,
  loading: () => (
    <div className="w-full h-full bg-[#0c0d12] flex flex-col items-center justify-center gap-4">
      <div className="w-10 h-10 border-4 border-emerald-400/25 border-t-emerald-400 rounded-full animate-spin" />
      <span className="text-sm font-mono text-white/40 uppercase tracking-widest animate-pulse">
        Inicializando Mapa...
      </span>
    </div>
  ),
});

const EMPTY_AI_REGIONS: string[] = [];

interface MapProps {
  regions: Region[];
  selectedRegion: Region | null;
  onSelectRegion: (region: Region) => void;
  activeIndicator: string;
  aiHighlightedRegions?: string[];
}

export default function InteractiveMap({
  regions,
  selectedRegion,
  onSelectRegion,
  activeIndicator,
  aiHighlightedRegions = EMPTY_AI_REGIONS,
}: MapProps) {
  const indicatorLabel =
    INDICATOR_LABELS[activeIndicator as IndicatorType] ?? activeIndicator;

  return (
    <div className="bg-[#0b0c10] border border-white/10 rounded-sm shadow-2xl flex flex-col h-full relative overflow-hidden">

      {/* Decorative corners */}
      <div className="absolute top-0 left-0 w-3 h-3 border-t-2 border-l-2 border-emerald-400/40 pointer-events-none z-10" />
      <div className="absolute top-0 right-0 w-3 h-3 border-t-2 border-r-2 border-emerald-400/40 pointer-events-none z-10" />
      <div className="absolute bottom-0 left-0 w-3 h-3 border-b-2 border-l-2 border-emerald-400/40 pointer-events-none z-10" />
      <div className="absolute bottom-0 right-0 w-3 h-3 border-b-2 border-r-2 border-emerald-400/40 pointer-events-none z-10" />

      {/* Header */}
      <div className="flex items-center justify-between px-5 pt-4 pb-3 border-b border-white/8 flex-shrink-0">
        <div className="flex items-center gap-2.5">
          <span className="w-2 h-2 bg-emerald-400 rounded-full animate-pulse flex-shrink-0" />
          <MapIcon className="w-4 h-4 text-emerald-400 flex-shrink-0" />
          <h2 className="text-sm font-bold text-white uppercase tracking-wider font-mono">
            Mapa de Brechas
          </h2>
        </div>
        <span className="text-xs text-emerald-400 font-mono font-semibold bg-emerald-400/8 border border-emerald-400/20 px-2.5 py-1 rounded-sm uppercase tracking-wider">
          {indicatorLabel}
        </span>
      </div>

      {/* AI highlight banner */}
      {aiHighlightedRegions.length > 0 && (
        <div className="mx-4 mt-3 bg-emerald-500/5 border border-emerald-500/20 px-3 py-2 rounded-sm flex items-start gap-2 flex-shrink-0">
          <Sparkles className="w-3.5 h-3.5 text-emerald-400 flex-shrink-0 mt-0.5" />
          <div className="flex flex-wrap gap-1.5 min-w-0">
            <span className="text-[10px] font-mono text-emerald-400 uppercase tracking-widest font-bold flex-shrink-0">
              IA activa:
            </span>
            {aiHighlightedRegions.map((rid) => (
              <span
                key={rid}
                className="text-[10px] bg-emerald-400/10 border border-emerald-400/25 text-emerald-300 px-1.5 py-0.5 rounded-sm uppercase tracking-wider font-mono"
              >
                {rid}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* Map — takes all remaining space, no explicit height needed */}
      <div className="flex-1 relative overflow-hidden min-h-[250px] lg:min-h-[380px]">
        <MapComponent
          regions={regions}
          selectedRegion={selectedRegion}
          activeIndicator={activeIndicator}
          onSelectRegion={onSelectRegion}
          aiHighlightedRegions={aiHighlightedRegions}
        />
      </div>

      {/* Legend — always at bottom, horizontal on all screen sizes */}
      <div className="px-5 py-3 border-t border-white/6 flex flex-wrap items-center gap-x-5 gap-y-2 flex-shrink-0">
        <span className="text-[10px] font-mono text-white/30 uppercase tracking-widest">
          Brecha:
        </span>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-sm bg-rose-500/30 border border-rose-500/50" />
          <span className="text-xs text-white/55">Severa &gt;50%</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-sm bg-amber-500/20 border border-amber-500/40" />
          <span className="text-xs text-white/55">Moderada 35–50%</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-3 h-3 rounded-sm bg-emerald-500/20 border border-emerald-500/35" />
          <span className="text-xs text-white/55">Leve &lt;35%</span>
        </div>
        <div className="flex items-center gap-2 ml-auto">
          <div
            className="w-2.5 h-2.5 rounded-full bg-emerald-400"
            style={{ boxShadow: "0 0 6px #10b981" }}
          />
          <span className="text-[10px] text-white/40 font-mono">Antena CDR</span>
        </div>
      </div>
    </div>
  );
}
