"use client";

import React from "react";
import { RefreshCw } from "lucide-react";

type ErrorType = "connection" | "timeout" | "server" | "unknown";

function classifyError(message: string): ErrorType {
  const msg = message.toLowerCase();
  if (msg.includes("no se pudo conectar") || msg.includes("verifique su conexión")) return "connection";
  if (msg.includes("tardó demasiado") || msg.includes("timeout")) return "timeout";
  if (msg.includes("no está disponible temporalmente") || msg.includes("servidor no")) return "server";
  return "unknown";
}

function getErrorContent(type: ErrorType): { title: string; description: string; color: string } {
  switch (type) {
    case "connection":
      return {
        title: "No se pudieron cargar los datos",
        description: "Los datos del territorio no se pudieron obtener del servidor. Verifique su conexión a internet y recargue la página.",
        color: "rose",
      };
    case "timeout":
      return {
        title: "La conexión tardó demasiado",
        description: "La solicitud tardó más de lo esperado. Verifique su conexión a internet e intente de nuevo.",
        color: "amber",
      };
    case "server":
      return {
        title: "Algo salió mal",
        description: "Ocurrió un error al cargar la información. Recargue la página para intentar de nuevo.",
        color: "rose",
      };
    default:
      return {
        title: "Algo salió mal",
        description: "Ocurrió un error inesperado. Recargue la página para intentar de nuevo.",
        color: "rose",
      };
  }
}

function ConnectionIllustration() {
  return (
    <svg viewBox="0 0 240 180" fill="none" className="w-full max-w-[240px] mx-auto">
      {/* Desk */}
      <rect x="40" y="120" width="160" height="8" rx="3" fill="currentColor" fillOpacity="0.12" />
      <rect x="50" y="128" width="6" height="30" rx="1" fill="currentColor" fillOpacity="0.08" />
      <rect x="184" y="128" width="6" height="30" rx="1" fill="currentColor" fillOpacity="0.08" />

      {/* Monitor */}
      <rect x="75" y="60" width="90" height="56" rx="4" stroke="currentColor" strokeOpacity="0.25" strokeWidth="1.5" fill="currentColor" fillOpacity="0.04" />
      <rect x="80" y="65" width="80" height="46" rx="2" fill="currentColor" fillOpacity="0.06" />
      {/* Monitor stand */}
      <rect x="112" y="116" width="16" height="6" rx="1" fill="currentColor" fillOpacity="0.1" />

      {/* Screen content - error message */}
      <rect x="92" y="78" width="56" height="6" rx="1" fill="currentColor" fillOpacity="0.15" />
      <rect x="100" y="90" width="40" height="4" rx="1" fill="currentColor" fillOpacity="0.08" />
      <rect x="104" y="98" width="32" height="4" rx="1" fill="currentColor" fillOpacity="0.08" />

      {/* X on screen */}
      <line x1="112" y1="106" x2="118" y2="112" stroke="currentColor" strokeOpacity="0.5" strokeWidth="1.5" strokeLinecap="round" />
      <line x1="118" y1="106" x2="112" y2="112" stroke="currentColor" strokeOpacity="0.5" strokeWidth="1.5" strokeLinecap="round" />

      {/* Disconnected cable */}
      <path d="M165 90 C180 90 185 85 190 80" stroke="currentColor" strokeOpacity="0.3" strokeWidth="1.5" strokeDasharray="4 3" />
      <rect x="188" y="76" width="12" height="8" rx="2" stroke="currentColor" strokeOpacity="0.3" strokeWidth="1" fill="currentColor" fillOpacity="0.06" />
      {/* Cable end - unplugged */}
      <path d="M165 90 C160 95 158 100 155 105" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1.5" strokeDasharray="3 3" />
      <circle cx="154" cy="107" r="3" stroke="currentColor" strokeOpacity="0.3" strokeWidth="1" fill="currentColor" fillOpacity="0.05" />

      {/* Wifi waves - broken */}
      <path d="M100 48 C110 38 130 38 140 48" stroke="currentColor" strokeOpacity="0.1" strokeWidth="1.5" strokeDasharray="3 3" />
      <path d="M106 42 C112 35 128 35 134 42" stroke="currentColor" strokeOpacity="0.08" strokeWidth="1.5" strokeDasharray="3 3" />

      {/* Plant on left */}
      <rect x="42" y="108" width="10" height="12" rx="2" fill="currentColor" fillOpacity="0.08" />
      <path d="M47 108 C42 100 50 95 47 88" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1.5" fill="none" />
      <path d="M47 100 C55 95 52 88 55 82" stroke="currentColor" strokeOpacity="0.12" strokeWidth="1.5" fill="none" />
      <circle cx="47" cy="86" r="4" fill="currentColor" fillOpacity="0.08" />
      <circle cx="55" cy="80" r="3" fill="currentColor" fillOpacity="0.06" />

      {/* Coffee cup on right */}
      <rect x="178" y="110" width="12" height="10" rx="1.5" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1" fill="currentColor" fillOpacity="0.04" />
      <path d="M190 113 C195 113 195 118 190 118" stroke="currentColor" strokeOpacity="0.12" strokeWidth="1" fill="none" />
      {/* Steam */}
      <path d="M182 108 C181 104 183 102 182 98" stroke="currentColor" strokeOpacity="0.08" strokeWidth="1" fill="none" />
      <path d="M186 108 C185 104 187 102 186 98" stroke="currentColor" strokeOpacity="0.06" strokeWidth="1" fill="none" />

      {/* Papers on desk */}
      <rect x="58" y="114" width="14" height="6" rx="0.5" fill="currentColor" fillOpacity="0.06" transform="rotate(-5 58 114)" />
      <rect x="158" y="113" width="10" height="5" rx="0.5" fill="currentColor" fillOpacity="0.05" transform="rotate(8 158 113)" />
    </svg>
  );
}

function TimeoutIllustration() {
  return (
    <svg viewBox="0 0 240 180" fill="none" className="w-full max-w-[240px] mx-auto">
      {/* Person sitting */}
      {/* Head */}
      <circle cx="90" cy="68" r="12" fill="currentColor" fillOpacity="0.1" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1" />
      {/* Body */}
      <path d="M90 80 L90 115" stroke="currentColor" strokeOpacity="0.2" strokeWidth="2" />
      {/* Arms */}
      <path d="M90 90 C80 95 75 100 72 108" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1.5" fill="none" />
      <path d="M90 90 C100 95 105 100 108 108" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1.5" fill="none" />
      {/* Legs */}
      <path d="M90 115 L82 135" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1.5" fill="none" />
      <path d="M90 115 L98 135" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1.5" fill="none" />
      {/* Chair */}
      <path d="M70 100 C68 85 72 75 75 70" stroke="currentColor" strokeOpacity="0.1" strokeWidth="1.5" fill="none" />
      <rect x="75" y="118" width="30" height="4" rx="1" fill="currentColor" fillOpacity="0.08" />

      {/* Clock on wall */}
      <circle cx="170" cy="45" r="22" stroke="currentColor" strokeOpacity="0.25" strokeWidth="1.5" fill="currentColor" fillOpacity="0.04" />
      <circle cx="170" cy="45" r="18" fill="currentColor" fillOpacity="0.03" />
      {/* Clock hands - showing waiting */}
      <line x1="170" y1="45" x2="170" y2="32" stroke="currentColor" strokeOpacity="0.4" strokeWidth="1.5" strokeLinecap="round" />
      <line x1="170" y1="45" x2="180" y2="45" stroke="currentColor" strokeOpacity="0.3" strokeWidth="1" strokeLinecap="round" />
      <circle cx="170" cy="45" r="2" fill="currentColor" fillOpacity="0.4" />
      {/* Hour marks */}
      <line x1="170" y1="25" x2="170" y2="28" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1" />
      <line x1="170" y1="62" x2="170" y2="65" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1" />
      <line x1="150" y1="45" x2="153" y2="45" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1" />
      <line x1="187" y1="45" x2="190" y2="45" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1" />

      {/* Desk */}
      <rect x="120" y="100" width="100" height="6" rx="2" fill="currentColor" fillOpacity="0.1" />
      <rect x="130" y="106" width="4" height="28" rx="1" fill="currentColor" fillOpacity="0.06" />
      <rect x="206" y="106" width="4" height="28" rx="1" fill="currentColor" fillOpacity="0.06" />

      {/* Laptop on desk */}
      <rect x="145" y="82" width="50" height="18" rx="2" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1" fill="currentColor" fillOpacity="0.04" />
      <rect x="148" y="85" width="44" height="12" rx="1" fill="currentColor" fillOpacity="0.05" />
      {/* Loading spinner on screen */}
      <circle cx="170" cy="91" r="4" stroke="currentColor" strokeOpacity="0.3" strokeWidth="1" strokeDasharray="6 3" fill="none" />

      {/* Coffee cup */}
      <rect x="210" y="90" width="10" height="10" rx="1.5" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1" fill="currentColor" fillOpacity="0.04" />
      <path d="M220 93 C224 93 224 97 220 97" stroke="currentColor" strokeOpacity="0.1" strokeWidth="1" fill="none" />
      {/* Steam */}
      <path d="M213 88 C212 84 214 82 213 78" stroke="currentColor" strokeOpacity="0.08" strokeWidth="1" fill="none" />

      {/* Pile of papers */}
      <rect x="125" y="93" width="16" height="7" rx="0.5" fill="currentColor" fillOpacity="0.06" transform="rotate(-3 125 93)" />
      <rect x="126" y="91" width="14" height="6" rx="0.5" fill="currentColor" fillOpacity="0.05" transform="rotate(2 126 91)" />
      <rect x="127" y="89" width="12" height="5" rx="0.5" fill="currentColor" fillOpacity="0.04" transform="rotate(-1 127 89)" />

      {/* Thought bubble - waiting */}
      <circle cx="110" cy="52" r="3" fill="currentColor" fillOpacity="0.08" />
      <circle cx="118" cy="46" r="4" fill="currentColor" fillOpacity="0.06" />
      <ellipse cx="130" cy="38" rx="10" ry="7" fill="currentColor" fillOpacity="0.05" stroke="currentColor" strokeOpacity="0.1" strokeWidth="0.5" />
      <text x="126" y="41" fontSize="8" fill="currentColor" fillOpacity="0.2" fontFamily="monospace">...</text>
    </svg>
  );
}

function ServerIllustration() {
  return (
    <svg viewBox="0 0 240 180" fill="none" className="w-full max-w-[240px] mx-auto">
      {/* Server rack */}
      <rect x="60" y="20" width="80" height="140" rx="4" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1.5" fill="currentColor" fillOpacity="0.03" />

      {/* Server unit 1 */}
      <rect x="68" y="28" width="64" height="24" rx="2" fill="currentColor" fillOpacity="0.08" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1" />
      <circle cx="80" cy="40" r="2.5" fill="currentColor" fillOpacity="0.5" />
      <circle cx="88" cy="40" r="2.5" fill="currentColor" fillOpacity="0.3" />
      <rect x="96" y="36" width="28" height="3" rx="1" fill="currentColor" fillOpacity="0.12" />
      <rect x="96" y="42" width="20" height="2" rx="0.5" fill="currentColor" fillOpacity="0.08" />

      {/* Server unit 2 */}
      <rect x="68" y="58" width="64" height="24" rx="2" fill="currentColor" fillOpacity="0.08" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1" />
      <circle cx="80" cy="70" r="2.5" fill="currentColor" fillOpacity="0.5" />
      <circle cx="88" cy="70" r="2.5" fill="currentColor" fillOpacity="0.2" />
      <rect x="96" y="66" width="28" height="3" rx="1" fill="currentColor" fillOpacity="0.12" />
      <rect x="96" y="72" width="20" height="2" rx="0.5" fill="currentColor" fillOpacity="0.08" />

      {/* Server unit 3 - error */}
      <rect x="68" y="88" width="64" height="24" rx="2" fill="currentColor" fillOpacity="0.1" stroke="currentColor" strokeOpacity="0.25" strokeWidth="1" />
      <circle cx="80" cy="100" r="2.5" fill="currentColor" fillOpacity="0.6" />
      <circle cx="88" cy="100" r="2.5" fill="currentColor" fillOpacity="0.15" />
      {/* Error bars on unit 3 */}
      <rect x="96" y="96" width="28" height="3" rx="1" fill="currentColor" fillOpacity="0.2" />
      <rect x="96" y="102" width="20" height="2" rx="0.5" fill="currentColor" fillOpacity="0.15" />
      <rect x="96" y="106" width="14" height="2" rx="0.5" fill="currentColor" fillOpacity="0.1" />

      {/* Server unit 4 */}
      <rect x="68" y="118" width="64" height="24" rx="2" fill="currentColor" fillOpacity="0.08" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1" />
      <circle cx="80" cy="130" r="2.5" fill="currentColor" fillOpacity="0.4" />
      <circle cx="88" cy="130" r="2.5" fill="currentColor" fillOpacity="0.3" />
      <rect x="96" y="126" width="28" height="3" rx="1" fill="currentColor" fillOpacity="0.12" />
      <rect x="96" y="132" width="20" height="2" rx="0.5" fill="currentColor" fillOpacity="0.08" />

      {/* Cables coming out */}
      <path d="M140 40 C155 40 160 35 170 30" stroke="currentColor" strokeOpacity="0.12" strokeWidth="1.5" fill="none" />
      <path d="M140 70 C155 70 165 65 175 60" stroke="currentColor" strokeOpacity="0.1" strokeWidth="1.5" fill="none" />
      <path d="M140 100 C155 100 165 95 175 90" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1.5" fill="none" />
      <path d="M140 130 C155 130 160 125 170 120" stroke="currentColor" strokeOpacity="0.1" strokeWidth="1.5" fill="none" />

      {/* Warning sign on right */}
      <path d="M185 70 L200 55 L215 70 L210 70 L210 85 L190 85 L190 70 Z" fill="currentColor" fillOpacity="0.06" stroke="currentColor" strokeOpacity="0.2" strokeWidth="1" />
      <line x1="200" y1="62" x2="200" y2="72" stroke="currentColor" strokeOpacity="0.4" strokeWidth="1.5" strokeLinecap="round" />
      <circle cx="200" cy="78" r="1.2" fill="currentColor" fillOpacity="0.4" />

      {/* Floor line */}
      <line x1="40" y1="162" x2="200" y2="162" stroke="currentColor" strokeOpacity="0.08" strokeWidth="1" />

      {/* Small status box */}
      <rect x="160" y="140" width="30" height="18" rx="2" fill="currentColor" fillOpacity="0.04" stroke="currentColor" strokeOpacity="0.1" strokeWidth="0.5" />
      <rect x="164" y="144" width="8" height="2" rx="0.5" fill="currentColor" fillOpacity="0.15" />
      <rect x="164" y="148" width="6" height="2" rx="0.5" fill="currentColor" fillOpacity="0.1" />
      <rect x="164" y="152" width="10" height="2" rx="0.5" fill="currentColor" fillOpacity="0.08" />
    </svg>
  );
}

function UnknownIllustration() {
  return (
    <svg viewBox="0 0 240 180" fill="none" className="w-full max-w-[240px] mx-auto">
      {/* Desk */}
      <rect x="30" y="120" width="180" height="8" rx="3" fill="currentColor" fillOpacity="0.1" />
      <rect x="40" y="128" width="6" height="28" rx="1" fill="currentColor" fillOpacity="0.06" />
      <rect x="194" y="128" width="6" height="28" rx="1" fill="currentColor" fillOpacity="0.06" />

      {/* Scattered papers */}
      <rect x="45" y="108" width="20" height="12" rx="1" fill="currentColor" fillOpacity="0.06" transform="rotate(-12 45 108)" />
      <rect x="55" y="104" width="18" height="10" rx="1" fill="currentColor" fillOpacity="0.05" transform="rotate(8 55 104)" />
      <rect x="170" y="106" width="16" height="10" rx="1" fill="currentColor" fillOpacity="0.05" transform="rotate(-5 170 106)" />
      <rect x="180" y="110" width="14" height="8" rx="1" fill="currentColor" fillOpacity="0.04" transform="rotate(15 180 110)" />

      {/* Large question mark in center */}
      <path d="M108 50 C108 38 118 30 128 30 C138 30 148 38 148 50 C148 60 138 65 128 72 L128 80" stroke="currentColor" strokeOpacity="0.25" strokeWidth="2" fill="none" strokeLinecap="round" />
      <circle cx="128" cy="92" r="3" fill="currentColor" fillOpacity="0.25" />

      {/* Question mark glow */}
      <circle cx="128" cy="60" r="35" fill="currentColor" fillOpacity="0.03" />

      {/* Monitor */}
      <rect x="95" y="60" width="66" height="48" rx="3" stroke="currentColor" strokeOpacity="0.15" strokeWidth="1" fill="currentColor" fillOpacity="0.03" />
      <rect x="99" y="64" width="58" height="40" rx="2" fill="currentColor" fillOpacity="0.04" />
      <rect x="122" y="108" width="12" height="5" rx="1" fill="currentColor" fillOpacity="0.08" />

      {/* Screen content - garbled */}
      <rect x="108" y="72" width="30" height="3" rx="0.5" fill="currentColor" fillOpacity="0.1" />
      <rect x="112" y="78" width="22" height="3" rx="0.5" fill="currentColor" fillOpacity="0.08" />
      <rect x="116" y="84" width="16" height="3" rx="0.5" fill="currentColor" fillOpacity="0.06" />
      <rect x="108" y="90" width="30" height="3" rx="0.5" fill="currentColor" fillOpacity="0.08" />

      {/* Keyboard */}
      <rect x="105" y="114" width="46" height="6" rx="1" fill="currentColor" fillOpacity="0.06" />

      {/* Pencil */}
      <line x1="158" y1="112" x2="180" y2="100" stroke="currentColor" strokeOpacity="0.12" strokeWidth="2" strokeLinecap="round" />
      <circle cx="180" cy="100" r="1" fill="currentColor" fillOpacity="0.15" />

      {/* Sticky note */}
      <rect x="42" y="80" width="22" height="20" rx="1" fill="currentColor" fillOpacity="0.06" stroke="currentColor" strokeOpacity="0.1" strokeWidth="0.5" />
      <rect x="46" y="85" width="14" height="2" rx="0.5" fill="currentColor" fillOpacity="0.08" />
      <rect x="46" y="89" width="10" height="2" rx="0.5" fill="currentColor" fillOpacity="0.06" />
      <rect x="46" y="93" width="12" height="2" rx="0.5" fill="currentColor" fillOpacity="0.05" />

      {/* Small plant */}
      <rect x="190" y="108" width="8" height="12" rx="1.5" fill="currentColor" fillOpacity="0.06" />
      <path d="M194 108 C190 102 196 98 194 92" stroke="currentColor" strokeOpacity="0.1" strokeWidth="1" fill="none" />
      <circle cx="194" cy="90" r="3" fill="currentColor" fillOpacity="0.05" />
    </svg>
  );
}

function ErrorIllustration({ type }: { type: ErrorType }) {
  switch (type) {
    case "connection": return <ConnectionIllustration />;
    case "timeout": return <TimeoutIllustration />;
    case "server": return <ServerIllustration />;
    default: return <UnknownIllustration />;
  }
}

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const errorType = classifyError(error.message || "");
  const content = getErrorContent(errorType);
  const colorClass = content.color === "amber" ? "text-amber-400" : "text-rose-400";

  return (
    <div className="min-h-screen bg-[#060606] text-white/90 flex flex-col font-mono p-6 justify-center items-center">
      <div className="max-w-xl w-full bg-[#0b0c10] border border-white/10 rounded-sm p-8 space-y-5 shadow-2xl relative overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-[2px] bg-gradient-to-r from-transparent via-rose-500/50 to-transparent" />

        <div className={colorClass}>
          <ErrorIllustration type={errorType} />
        </div>

        <h1 className="text-base font-bold uppercase tracking-[0.2em] text-white font-mono text-center">
          {content.title}
        </h1>

        <p className="text-sm text-white/50 font-sans leading-relaxed text-center">
          {content.description}
        </p>

        <button
          type="button"
          onClick={() => reset()}
          className="w-full px-5 py-2.5 bg-white text-black hover:bg-white/90 font-bold text-sm rounded-sm transition-all cursor-pointer flex items-center justify-center gap-2 uppercase tracking-wider font-mono"
        >
          <RefreshCw className="w-4 h-4" />
          Reintentar
        </button>
      </div>
    </div>
  );
}
