"use client";

import React from "react";
import { Region } from "../lib/api";
import { 
  Users, 
  Wifi, 
  BookOpen, 
  GraduationCap, 
  MapPin
} from "lucide-react";

interface DetailProps {
  region: Region | null;
}

export default function DetailPanel({ region }: DetailProps) {
  if (!region) {
    return (
      <div className="bg-[#0b0c10] border border-white/10 rounded-sm p-4 lg:p-6 shadow-2xl flex flex-col justify-center items-center h-full min-h-[280px] lg:min-h-[460px] text-center font-mono">
        <MapPin className="w-12 h-12 text-emerald-500/40 mb-5 animate-pulse" />
        <h3 className="text-sm font-bold text-white/50 uppercase tracking-widest">
          Sin Región Seleccionada
        </h3>
        <p className="text-sm text-white/40 max-w-[240px] mt-3 leading-relaxed">
          Seleccione una de las zonas en el mapa cartográfico real para auditar los indicadores de red y demografía territorial.
        </p>
      </div>
    );
  }

  // Calculate some derived values dynamically to make it rich
  const connectivityStatus = region.metrics.connectivity >= 85 ? "Alta" : region.metrics.connectivity >= 65 ? "Media" : "Crítica";
  // gapSeverity derived for display use in tooltip/badge context
  const _gapSeverity = region.gapScore > 50 ? "Severo" : region.gapScore > 35 ? "Moderado" : "Controlado";

  return (
    <div className="bg-[#0b0c10] border border-white/10 rounded-sm p-4 lg:p-6 shadow-2xl flex flex-col h-full min-h-[280px] lg:min-h-[460px] justify-between relative overflow-hidden">
      
      {/* Visual background element: Cyber Grid */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#ffffff02_1px,transparent_1px),linear-gradient(to_bottom,#ffffff02_1px,transparent_1px)] bg-[size:20px_20px] pointer-events-none" />

      {/* Header Info */}
      <div className="space-y-4 relative z-10">
        <div className="flex items-center justify-between gap-3 border-b border-white/10 pb-4">
          <div>
            <span className="text-xs font-mono text-emerald-400 uppercase tracking-widest font-bold">
              Detalle Regional
            </span>
            <h3 className="text-lg font-bold text-white uppercase tracking-wider font-mono mt-1">
              {region.name || region.regionName || "Sin nombre"}
            </h3>
          </div>
          <div className="text-right">
            <span className="text-xs font-mono text-white/40 uppercase tracking-widest block font-bold">
              Brecha Digital
            </span>
            <span className={`text-lg font-mono font-bold ${region.gapScore > 50 ? "text-rose-400" : region.gapScore > 35 ? "text-amber-400" : "text-emerald-400"}`}>
              {region.gapScore}%
            </span>
          </div>
        </div>

        <p className="text-sm text-white/80 leading-relaxed font-sans">
          {region.details}
        </p>
      </div>

      {/* Core Indicators Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 my-6 relative z-10">
        
        {/* Population */}
        <div className="bg-black/60 p-4 rounded-sm border border-white/5 flex flex-col justify-between shadow-inner">
          <div className="flex items-center justify-between gap-2 text-white/50">
            <span className="text-xs font-mono uppercase tracking-widest">Población</span>
            <Users className="w-5 h-5 text-white/75" />
          </div>
          <div className="mt-4">
            <span className="text-lg font-bold font-mono tracking-tight text-white/95">
              {region.population.toLocaleString()}
            </span>
            <span className="text-xs text-white/40 block font-mono mt-1">Ciudadanos</span>
          </div>
        </div>

        {/* Connectivity */}
        <div className="bg-black/60 p-4 rounded-sm border border-white/5 flex flex-col justify-between shadow-inner">
          <div className="flex items-center justify-between gap-2 text-white/50">
            <span className="text-xs font-mono uppercase tracking-widest">Conectividad</span>
            <Wifi className="w-5 h-5 text-white/75" />
          </div>
          <div className="mt-4">
            <span className="text-lg font-bold font-mono tracking-tight text-white/95">
              {region.metrics.connectivity}%
            </span>
            <span className={`text-xs font-mono uppercase font-bold block mt-1 ${connectivityStatus === "Alta" ? "text-emerald-400" : connectivityStatus === "Media" ? "text-amber-400" : "text-rose-400"}`}>
              {connectivityStatus}
            </span>
          </div>
        </div>

        {/* Digital Literacy */}
        <div className="bg-black/60 p-4 rounded-sm border border-white/5 flex flex-col justify-between shadow-inner">
          <div className="flex items-center justify-between gap-2 text-white/50">
            <span className="text-xs font-mono uppercase tracking-widest">Alfabetización</span>
            <BookOpen className="w-5 h-5 text-white/75" />
          </div>
          <div className="mt-4">
            <span className="text-lg font-bold font-mono tracking-tight text-white/95">
              {region.metrics.digitalLiteracy}%
            </span>
            <span className="text-xs text-white/40 block font-mono mt-1">Escolarizada</span>
          </div>
        </div>

        {/* Mentors */}
        <div className="bg-black/60 p-4 rounded-sm border border-white/5 flex flex-col justify-between shadow-inner">
          <div className="flex items-center justify-between gap-2 text-white/50">
            <span className="text-xs font-mono uppercase tracking-widest">Mentores</span>
            <GraduationCap className="w-5 h-5 text-white/75" />
          </div>
          <div className="mt-4">
            <span className="text-lg font-bold font-mono tracking-tight text-white/95">
              {region.metrics.mentorEngagement}
            </span>
            <span className="text-xs text-white/40 block font-mono mt-1">Asignados</span>
          </div>
        </div>
      </div>

    </div>
  );
}
