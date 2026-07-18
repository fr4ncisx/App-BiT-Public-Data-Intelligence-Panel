"use client";

import React from "react";
import { useQuery } from "@tanstack/react-query";
import { apiFetch, DataSource } from "../lib/api";
import {
  Layers,
  ShieldCheck,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Clock,
  Loader2,
} from "lucide-react";

// ── Helpers ────────────────────────────────────────────────────────────────────

/** Maps backend sourceType to a human-readable Spanish label */
const getSourceTypeLabel = (type: string): string => {
  switch (type.toUpperCase()) {
    case "CONCENTRATION":  return "Concentración Poblacional";
    case "NETWORK":        return "Cobertura de Red";
    case "TRAINING":       return "Programas de Formación";
    case "SOCIAL":         return "Indicadores Sociales";
    case "SEED":           return "Línea de Base Homologada";
    default:               return type;
  }
};

/** Maps backend lastIngestionState to color and label */
const getIngestionStateProps = (state: DataSource["lastIngestionState"]) => {
  switch (state) {
    case "COMPLETED":   return { color: "text-emerald-400", dot: "bg-emerald-400", label: "Completado",  Icon: CheckCircle2 };
    case "FAILED":      return { color: "text-rose-400",    dot: "bg-rose-400",    label: "Fallido",     Icon: XCircle };
    case "IN_PROGRESS": return { color: "text-amber-400",   dot: "bg-amber-400",   label: "En Progreso", Icon: Loader2 };
    case "PENDING":     return { color: "text-white/40",    dot: "bg-white/20",    label: "Pendiente",   Icon: Clock };
  }
};

/** Maps backend confidenceLevel to a Spanish label */
const getConfidenceLabel = (level: DataSource["confidenceLevel"]): string => {
  switch (level) {
    case "HIGH":   return "Alta";
    case "MEDIUM": return "Media";
    case "LOW":    return "Baja";
    default:       return "—";
  }
};

/** Format ingestion date from ISO string */
const formatDate = (iso: string | null): string => {
  if (!iso) return "—";
  try {
    return new Date(iso).toLocaleDateString("es-AR", {
      day: "2-digit", month: "short", year: "numeric",
    });
  } catch {
    return iso;
  }
};

// ── Component ──────────────────────────────────────────────────────────────────

export default function IngestionStatus() {
  const { data: sourceData, isLoading, isError } = useQuery({
    queryKey: ["data-sources"],
    queryFn: async () => {
      const response = await apiFetch<{ sources: DataSource[] }>("/data/sources");
      return response.data.sources;
    },
  });

  return (
    <div className="bg-[#0b0c10] border border-white/10 rounded-sm p-4 lg:p-6 shadow-2xl flex flex-col h-full min-h-[250px] lg:min-h-[400px]">

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-white/10 mb-5">
        <div>
          <h2 className="text-sm font-bold text-white flex items-center gap-2.5 uppercase tracking-wider font-mono">
            <Layers className="w-5 h-5 text-emerald-400" />
            <span>Fuentes de Datos e Ingesta</span>
          </h2>
          <p className="text-sm text-white/50 mt-2 leading-relaxed">
            Registro oficial y trazabilidad de los conjuntos de datos que fundamentan la evidencia territorial
          </p>
        </div>
      </div>

      {/* Loading */}
      {isLoading && (
        <div className="flex-1 flex justify-center items-center py-12 text-white/40 text-sm font-mono uppercase tracking-wider">
          <div className="w-4 h-4 border-t-2 border-emerald-400 rounded-full animate-spin mr-3" />
          <span>Cargando fuentes de datos...</span>
        </div>
      )}

      {/* Error */}
      {isError && (
        <div className="flex-1 flex flex-col justify-center items-center py-10 border border-white/5 bg-black/20 p-5 rounded-sm text-center font-mono">
          <AlertCircle className="w-8 h-8 text-rose-500 mb-3 animate-pulse" />
          <h3 className="text-sm font-bold text-white uppercase tracking-wider mb-2">No se pudo actualizar el estado</h3>
          <p className="text-sm text-white/40 max-w-sm leading-relaxed">
            No se pudo obtener el estado de las fuentes de datos. Verifique su conexión a internet e intente de nuevo.
          </p>
        </div>
      )}

      {/* Content */}
      {!isLoading && !isError && sourceData && (
        <div className="flex-1 space-y-5">

          {/* Sources Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
            {sourceData.slice(0, 3).map((src) => {
              const stateProps = getIngestionStateProps(src.lastIngestionState);
              const StateIcon = stateProps.Icon;
              const totalRows = src.rowsRead > 0 ? src.rowsRead : 1;
              const acceptanceRate = Math.round((src.rowsInserted / totalRows) * 100);

              return (
                <div
                  key={src.fileName}
                  className="bg-black/60 p-4 rounded-sm border border-white/5 hover:border-white/10 transition-colors flex flex-col justify-between shadow-inner"
                >
                  {/* Source type badge + state */}
                  <div className="flex items-center justify-between gap-2 mb-3">
                    <span className="text-xs bg-white/5 border border-white/10 text-white/50 px-2 py-0.5 rounded-sm font-mono uppercase tracking-wider truncate max-w-[150px]">
                      {getSourceTypeLabel(src.sourceType)}
                    </span>
                    <span className={`text-xs font-mono font-semibold flex items-center gap-1.5 flex-shrink-0 ${stateProps.color}`}>
                      <span className={`w-2 h-2 rounded-full flex-shrink-0 ${stateProps.dot} ${src.lastIngestionState === "IN_PROGRESS" ? "animate-pulse" : ""}`} />
                      {stateProps.label}
                    </span>
                  </div>

                  {/* Name */}
                  <h3 className="text-sm font-bold text-white mb-1 leading-snug line-clamp-1 overflow-hidden">{src.sourceName}</h3>
                  <p className="text-xs text-white/40 mb-3 leading-relaxed line-clamp-2">{src.description}</p>

                  {/* Stats row */}
                  <div className="grid grid-cols-3 gap-2 text-center mb-3">
                    <div className="bg-white/[0.03] rounded-sm p-1.5 border border-white/5 min-w-0 overflow-hidden">
                      <div className="text-xs font-bold text-white truncate">{src.rowsRead.toLocaleString()}</div>
                      <div className="text-[10px] text-white/40 uppercase tracking-wider">Leídos</div>
                    </div>
                    <div className="bg-emerald-500/5 rounded-sm p-1.5 border border-emerald-500/10 min-w-0 overflow-hidden">
                      <div className="text-xs font-bold text-emerald-400 truncate">{src.rowsInserted.toLocaleString()}</div>
                      <div className="text-[10px] text-white/40 uppercase tracking-wider">Insertados</div>
                    </div>
                    <div className={`rounded-sm p-1.5 border min-w-0 overflow-hidden ${src.rowsRejected > 0 ? "bg-rose-500/5 border-rose-500/10" : "bg-white/[0.03] border-white/5"}`}>
                      <div className={`text-xs font-bold truncate ${src.rowsRejected > 0 ? "text-rose-400" : "text-white/50"}`}>{src.rowsRejected.toLocaleString()}</div>
                      <div className="text-[10px] text-white/40 uppercase tracking-wider">Rechazados</div>
                    </div>
                  </div>

                  {/* Footer: date + confidence + acceptance rate */}
                  <div className="pt-3 border-t border-white/5 space-y-1.5">
                    <div className="flex items-center justify-between text-xs text-white/40 font-mono">
                      <span>Última ingesta:</span>
                      <span className="text-white/70" suppressHydrationWarning>{formatDate(src.lastIngestionFinishedAt)}</span>
                    </div>
                    <div className="flex items-center justify-between text-xs text-white/40 font-mono">
                      <span>Confianza:</span>
                      <span className="text-white/70">{getConfidenceLabel(src.confidenceLevel)}</span>
                    </div>
                    {/* Acceptance rate progress bar */}
                    <div className="mt-1">
                      <div className="flex justify-between text-[10px] text-white/30 font-mono mb-0.5">
                        <span>Tasa de aceptación</span>
                        <span>{acceptanceRate}%</span>
                      </div>
                      <div className="w-full h-1 bg-white/5 rounded-full overflow-hidden">
                        <div
                          className={`h-full rounded-full ${acceptanceRate >= 95 ? "bg-emerald-400" : acceptanceRate >= 80 ? "bg-amber-400" : "bg-rose-400"}`}
                          style={{ width: `${acceptanceRate}%` }}
                        />
                      </div>
                    </div>
                    {/* Error message if any */}
                    {src.errorMessage && (
                      <div className="flex items-start gap-1.5 text-xs text-rose-400 bg-rose-500/5 border border-rose-500/10 p-2 rounded-sm mt-1">
                        <StateIcon className="w-3 h-3 mt-0.5 flex-shrink-0" />
                        <span className="line-clamp-2 min-w-0">{src.errorMessage}</span>
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Privacy Protocol Banner */}
          <div className="bg-white/[0.02] border border-white/10 p-5 rounded-sm flex flex-col md:flex-row items-start md:items-center gap-4">
            <div className="p-2.5 bg-white/5 text-white/80 border border-white/10 rounded-sm flex-shrink-0">
              <ShieldCheck className="w-6 h-6 text-emerald-400" />
            </div>
            <div className="space-y-1">
              <h4 className="text-sm font-bold text-white/90 uppercase tracking-widest font-mono">
                Protocolo de Privacidad y Calidad de Evidencia
              </h4>
              <p className="text-sm text-white/50 leading-relaxed max-w-4xl">
                Los conjuntos de datos han sido rigurosamente sometidos a un protocolo de <strong>k-anonimato</strong> para salvaguardar la privacidad individual de los ciudadanos, asegurando que ningún identificador personal sea procesado por la plataforma. Se aplican filtros geo-espaciales estrictos para garantizar la consistencia analítica de los indicadores públicos presentados.
              </p>
            </div>
          </div>

        </div>
      )}

    </div>
  );
}
