"use client";

import React from "react";
import { Region, INDICATOR_LABELS, IndicatorType } from "../lib/api";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  Cell
} from "recharts";
import { BarChart3 } from "lucide-react";

interface ChartRecord {
  id: string;
  name: string;
  fullName: string;
  value: number;
  rawRegion: Region;
}

// Must be defined at module scope — react-hooks/static-components rule
const CustomTooltip = ({
  active,
  payload,
  activeIndicator,
}: {
  active?: boolean;
  payload?: Array<{ payload?: ChartRecord }>;
  activeIndicator: string;
}) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    if (!data) return null;
    const isMentors = activeIndicator === "MENTORSHIP";
    return (
      <div className="bg-[#0f111a] border border-white/10 p-3 shadow-2xl font-mono text-sm space-y-1 rounded-sm">
        <p className="font-bold text-white uppercase">{data.fullName}</p>
        <p className="text-white/50 uppercase tracking-widest text-xs">
          {INDICATOR_LABELS[activeIndicator as IndicatorType] ?? activeIndicator}:{" "}
          <span className="text-white font-bold">{data.value}{isMentors ? "" : "%"}</span>
        </p>
      </div>
    );
  }
  return null;
};

interface ChartProps {
  regions: Region[];
  activeIndicator: string;
  onSelectRegion: (region: Region) => void;
  selectedRegion: Region | null;
}

export default function IndicatorsChart({ 
  regions, 
  activeIndicator, 
  onSelectRegion,
  selectedRegion
}: ChartProps) {

  // Prepare chart-friendly data based on active indicator (uses backend IndicatorType keys)
  const chartData = regions.map((r) => {
    let value = r.gapScore;
    const m = r.metrics;

    if (activeIndicator === "EMPLOYABILITY") {
      value = m?.connectivity ?? r.gapScore;
    } else if (activeIndicator === "TRAINING") {
      value = m?.digitalLiteracy ?? r.gapScore;
    } else if (activeIndicator === "MENTORSHIP") {
      value = m?.mentorEngagement ?? r.gapScore;
    } else if (activeIndicator === "MENTAL_HEALTH" || activeIndicator === "SOCIAL_EXPERIENCE") {
      value = m?.socialIndex ?? r.gapScore;
    }

    return {
      id: r.id,
      name: (r.name || r.regionName || "Sin nombre").split(" ")[0],
      fullName: r.name || r.regionName || "Sin nombre",
      value,
      rawRegion: r,
    };
  });

  const getValueLabel = () => {
    if (activeIndicator === "MENTORSHIP") return "Mentores Activos";
    return "% Cobertura / Nivel";
  };

  return (
    <div className="bg-[#0b0c10] border border-white/10 rounded-sm p-4 lg:p-6 shadow-2xl flex flex-col h-full min-h-[200px] lg:min-h-[300px]">
      
      {/* Header */}
      <div className="flex items-center justify-between pb-4 border-b border-white/10 mb-5">
        <div>
          <h2 className="text-sm font-bold text-white flex items-center gap-2.5 uppercase tracking-wider font-mono">
            <BarChart3 className="w-5 h-5 text-emerald-400" />
            <span>Métricas Comparativas</span>
          </h2>
          <p className="text-sm text-white/50 mt-2 leading-relaxed">
            Análisis de distribución de {activeIndicator} por región de Florianópolis
          </p>
        </div>
      </div>

      {/* Chart Wrapper */}
      <div className="flex-1 w-full min-h-[180px]">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart
            data={chartData}
            margin={{ top: 5, right: 10, left: -20, bottom: 5 }}
            onClick={(state) => {
              if (state && state.activePayload) {
                onSelectRegion(state.activePayload[0].payload.rawRegion);
              }
            }}
          >
            <XAxis 
              dataKey="name" 
              stroke="#ffffff44" 
              fontSize={12}
              tickLine={false}
              fontFamily="monospace"
            />
            <YAxis 
              stroke="#ffffff44" 
              fontSize={12}
              tickLine={false}
              fontFamily="monospace"
              domain={[0, activeIndicator === "MENTORSHIP" ? "auto" : 100]}
            />
            <Tooltip
              content={(props) => <CustomTooltip {...props} activeIndicator={activeIndicator} />}
              cursor={{ fill: "rgba(255,255,255,0.02)" }}
            />
            <Bar dataKey="value" radius={[2, 2, 0, 0]}>
              {chartData.map((entry) => {
                const isSelected = selectedRegion?.id === entry.id;
                return (
                  <Cell 
                    key={`cell-${entry.id}`} 
                    className="transition-all duration-300 cursor-pointer"
                    fill={isSelected ? "#34d399" : "rgba(255,255,255,0.15)"}
                    stroke={isSelected ? "#34d399" : "rgba(255,255,255,0.3)"}
                    strokeWidth={isSelected ? 2 : 0.5}
                  />
                );
              })}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Footer Meta */}
      <div className="mt-3 pt-3 border-t border-white/5 flex items-center justify-between text-xs text-white/35 font-mono uppercase tracking-widest">
        <span>Fuente: ANATEL / IBGE / Red Mentores</span>
        <span className="text-white/50">{getValueLabel()}</span>
      </div>

    </div>
  );
}
