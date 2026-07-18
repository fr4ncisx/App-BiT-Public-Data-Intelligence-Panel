"use client";

import React from "react";

export default function DashboardHeader() {
  return (
    <header className="border-b border-white/10 bg-black/45 backdrop-blur-md sticky top-0 z-40 px-4 sm:px-6 py-3 sm:py-4 flex items-center justify-between">
      {/* Brand */}
      <div className="flex items-center gap-3.5">
        <div className="w-9 h-9 bg-white text-black flex items-center justify-center font-bold font-mono tracking-tighter text-sm rounded-sm">
          BiT
        </div>
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-base font-bold tracking-[0.25em] text-white uppercase font-mono">
              BiT.INTELLIGENCE
            </h1>
          </div>
          <p className="text-sm text-white/50 font-mono uppercase tracking-wider mt-1 hidden sm:block">
            Evidencia Territorial & Brechas Socio-Digitales
          </p>
        </div>
      </div>
    </header>
  );
}
