"use client";

import React, { useState, useEffect, useRef } from "react";
import { apiFetch, AiAnswer, apiFetchSuggestedQuestions } from "../lib/api";
import {
  Sparkles,
  Send,
  AlertCircle,
  MapPin,
  Map as MapIcon,
  RefreshCw,
  TrendingUp,
  Users,
  Wifi,
  BookOpen,
  ChevronRight,
  User,
  Bot,
} from "lucide-react";

// ── Types ───────────────────────────────────────────────────────────────────

interface ChatMessage {
  id: string;
  role: "user" | "assistant";
  query?: string;
  answer?: AiAnswer;
  loading?: boolean;
  error?: string;
}

// ── Constants ─────────────────────────────────────────────────────────────────

const normalizeRegionCode = (code: string): string => {
  const u = code.toUpperCase();
  if (u.includes("CBD") || u.includes("BEIRAMAR") || u.includes("TRINDADE") || u.includes("CENTRO") || u.includes("HISTORICO")) return "centro";
  if (u.includes("SC401") || u.includes("INGLESES") || u.includes("JURERE") || u.includes("NORTE")) return "norte";
  if (u.includes("LAGOA") || u.includes("BARRA") || u.includes("LESTE")) return "leste";
  if (u.includes("CAMPECHE") || u.includes("SANTO_AMARO") || u.includes("SUL") || u.includes("GOV_CELSO")) return "sul";
  if (u.includes("ESTREITO") || u.includes("CONTINENTE") || u.includes("SAO_JOSE") || u.includes("PALHOCA")) return "continente";
  const l = code.toLowerCase();
  if (l.includes("norte")) return "norte";
  if (l.includes("centro") || l.includes("historico")) return "centro";
  if (l.includes("sul") || l.includes("campeche")) return "sul";
  if (l.includes("leste") || l.includes("lagoa")) return "leste";
  if (l.includes("continente") || l.includes("estreito")) return "continente";
  return "";
};

const SUGGESTED_ICONS: React.FC<{ className?: string }>[] = [
  TrendingUp,
  Users,
  Wifi,
  BookOpen,
];

const SUGGESTED_CATEGORIES = ["Brechas", "Mentoría", "Conectividad", "Formación"];

let msgCounter = 0;
const nextId = () => `msg-${++msgCounter}-${Date.now()}`;

// ── Props ─────────────────────────────────────────────────────────────────────

interface IntelligenceQueryProps {
  onNavigateToRegion?: (regionName: string) => void;
  onAiAnswer?: (regions: string[]) => void;
}

// ── Component ─────────────────────────────────────────────────────────────────

export default function IntelligenceQuery({ onNavigateToRegion, onAiAnswer }: IntelligenceQueryProps) {
  const [query, setQuery] = useState("");
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const [suggestionsLoading, setSuggestionsLoading] = useState(true);
  const [showAllRegions, setShowAllRegions] = useState<Record<string, boolean>>({});
  const chatEndRef = useRef<HTMLDivElement>(null);
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const isUserAtBottomRef = useRef(true);

  useEffect(() => {
    apiFetchSuggestedQuestions().then((qs) => {
      setSuggestions(qs);
      setSuggestionsLoading(false);
    });
  }, []);

  // Track if user is near bottom of chat
  const handleScroll = () => {
    const el = chatContainerRef.current;
    if (!el) return;
    isUserAtBottomRef.current = el.scrollHeight - el.scrollTop - el.clientHeight < 80;
  };

  // Auto-scroll only if user was already near bottom
  useEffect(() => {
    if (isUserAtBottomRef.current) {
      chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  const handleQuerySubmit = async (text: string) => {
    const trimmed = text.trim();
    if (!trimmed) return;

    const userMsgId = nextId();
    const assistantMsgId = nextId();

    // Add user message + loading assistant message
    isUserAtBottomRef.current = true; // Force scroll on user action
    setMessages((prev) => [
      ...prev,
      { id: userMsgId, role: "user", query: trimmed },
      { id: assistantMsgId, role: "assistant", loading: true },
    ]);
    setQuery("");
    // Force scroll to bottom after user sends message
    setTimeout(() => chatEndRef.current?.scrollIntoView({ behavior: "smooth" }), 50);

    try {
      const res = await apiFetch<AiAnswer>("/data/queries", {
        method: "POST",
        body: JSON.stringify({ query: trimmed, language: "ES" }),
      });
      const ans = res.data;

      setMessages((prev) =>
        prev.map((m) =>
          m.id === assistantMsgId ? { ...m, loading: false, answer: ans } : m
        )
      );

      if (ans.regions?.length) {
        const zones = [...new Set(ans.regions.flatMap((r) => {
          const code = normalizeRegionCode(r.regionCode);
          return code ? [code] : [];
        }))];
        onAiAnswer?.(zones);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "";
      const isUnsafe = !msg || msg.includes("Failed to fetch") || msg.includes("TypeError")
        || msg.includes("ERR_NETWORK") || msg.includes("stack") || /at\s+/.test(msg);
      setMessages((prev) =>
        prev.map((m) =>
          m.id === assistantMsgId
            ? {
                ...m,
                loading: false,
                error: isUnsafe
                  ? "No se pudo procesar la consulta. Verifique su conexión a internet e intente de nuevo."
                  : msg,
              }
            : m
        )
      );
    }
  };

  const handleClear = () => {
    setMessages([]);
    onAiAnswer?.([]);
  };

  const handleRetry = (queryText: string) => {
    setMessages((prev) => prev.slice(0, -2)); // remove last user+error messages
    handleQuerySubmit(queryText);
  };

  const isEmpty = messages.length === 0;

  return (
    <div className="bg-[#0b0c10] border border-white/10 rounded-sm shadow-2xl flex flex-col h-full min-h-[300px] lg:min-h-[500px] relative overflow-hidden">

      {/* Subtle grid background */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#ffffff02_1px,transparent_1px),linear-gradient(to_bottom,#ffffff02_1px,transparent_1px)] bg-[size:20px_20px] pointer-events-none" />

      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <div className="px-5 pt-4 pb-3 border-b border-white/8 flex-shrink-0 relative z-10">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <Sparkles className="w-4 h-4 text-emerald-400 flex-shrink-0" />
            <h2 className="text-sm font-bold text-white uppercase tracking-wider font-mono">
              Análisis IA
            </h2>
          </div>
          {!isEmpty && (
            <button
              type="button"
              onClick={handleClear}
              className="text-[10px] font-mono text-white/30 hover:text-white/60 uppercase tracking-widest transition-colors cursor-pointer"
            >
              Nueva conversación
            </button>
          )}
        </div>
        {isEmpty && (
          <p className="text-xs text-white/45 mt-1.5 font-sans leading-relaxed">
            Haga una pregunta sobre brechas, zonas o indicadores del territorio.
          </p>
        )}
      </div>

      {/* ── Body (scrollable) ───────────────────────────────────────────────── */}
      <div ref={chatContainerRef} onScroll={handleScroll} className="flex-1 overflow-y-auto min-h-0 relative z-10">

        {/* Empty state — guide + suggested queries */}
        {isEmpty && (
          <div className="p-4 space-y-4">

            {/* How-to-use mini guide */}
            <div className="bg-emerald-500/5 border border-emerald-500/15 rounded-sm px-4 py-3 space-y-2">
              <span className="text-[10px] font-mono text-emerald-400 uppercase tracking-widest font-bold block">
                Cómo usarlo
              </span>
              <ul className="space-y-1.5 text-xs text-white/55 font-sans">
                <li className="flex items-start gap-2">
                  <ChevronRight className="w-3 h-3 text-emerald-400/60 flex-shrink-0 mt-0.5" />
                  Escriba una pregunta en lenguaje natural sobre el territorio
                </li>
                <li className="flex items-start gap-2">
                  <ChevronRight className="w-3 h-3 text-emerald-400/60 flex-shrink-0 mt-0.5" />
                  La IA analiza los indicadores y destaca las zonas relevantes en el mapa
                </li>
                <li className="flex items-start gap-2">
                  <ChevronRight className="w-3 h-3 text-emerald-400/60 flex-shrink-0 mt-0.5" />
                  Haga clic en una región del resultado para navegar al mapa
                </li>
              </ul>
            </div>

            {/* Categorized suggestions */}
            <div className="space-y-2">
              <span className="text-[10px] font-mono text-white/30 uppercase tracking-widest font-bold block">
                Preguntas frecuentes
              </span>
              {suggestionsLoading ? (
                <div className="flex flex-col gap-2">
                  {[0, 1, 2, 3].map((i) => (
                    <div key={i} className="bg-white/[0.02] border border-white/6 px-3.5 py-3 rounded-sm animate-pulse">
                      <div className="h-3 w-24 bg-white/8 rounded-sm mb-2" />
                      <div className="h-3 w-full bg-white/8 rounded-sm" />
                    </div>
                  ))}
                </div>
              ) : (
                <div className="flex flex-col gap-2">
                  {suggestions.map((text, idx) => {
                    const Icon = SUGGESTED_ICONS[idx % SUGGESTED_ICONS.length];
                    const category = SUGGESTED_CATEGORIES[idx % SUGGESTED_CATEGORIES.length];
                    return (
                      <button
                        type="button"
                        key={text}
                        onClick={() => handleQuerySubmit(text)}
                        className="group text-left bg-white/[0.02] hover:bg-emerald-500/5 border border-white/6 hover:border-emerald-500/20 px-3.5 py-3 rounded-sm transition-all cursor-pointer"
                      >
                        <div className="flex items-center gap-2 mb-1">
                          <Icon className="w-3.5 h-3.5 text-white/35 group-hover:text-emerald-400 flex-shrink-0 transition-colors" />
                          <span className="text-[10px] font-mono text-white/35 group-hover:text-emerald-400/70 uppercase tracking-widest transition-colors">
                            {category}
                          </span>
                        </div>
                        <p className="text-xs text-white/65 group-hover:text-white/85 font-sans leading-relaxed transition-colors">
                          {text}
                        </p>
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Chat thread */}
        {!isEmpty && (
          <div className="p-4 space-y-4">
            {messages.map((msg) => (
              <div key={msg.id} className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}>
                {msg.role === "user" ? (
                  /* User bubble — right aligned */
                  <div className="max-w-[85%] bg-emerald-500/10 border border-emerald-500/20 rounded-sm px-3.5 py-2.5 rounded-br-sm">
                    <div className="flex items-center gap-1.5 mb-1">
                      <User className="w-3 h-3 text-emerald-400/60" />
                      <span className="text-[10px] font-mono text-emerald-400/50 uppercase tracking-widest">Tú</span>
                    </div>
                    <p className="text-xs text-white/80 font-sans leading-relaxed">
                      {msg.query}
                    </p>
                  </div>
                ) : (
                  /* Assistant bubble — left aligned */
                  <div className="max-w-[90%] space-y-2">
                    {msg.loading ? (
                      /* Loading state */
                      <div className="bg-black/50 border border-white/5 rounded-sm px-4 py-3 rounded-bl-sm">
                        <div className="flex items-center gap-2 mb-2">
                          <Bot className="w-3 h-3 text-emerald-400/60" />
                          <div className="flex items-center gap-1.5">
                            <div className="w-1.5 h-1.5 rounded-full bg-emerald-400/40 animate-pulse" />
                            <div className="w-1.5 h-1.5 rounded-full bg-emerald-400/30 animate-pulse" style={{ animationDelay: "0.2s" }} />
                            <div className="w-1.5 h-1.5 rounded-full bg-emerald-400/20 animate-pulse" style={{ animationDelay: "0.4s" }} />
                          </div>
                          <span className="text-[10px] font-mono text-white/30 uppercase tracking-widest">
                            Analizando...
                          </span>
                        </div>
                        <div className="space-y-2">
                          <div className="h-3 w-full bg-white/6 rounded-sm" />
                          <div className="h-3 w-5/6 bg-white/6 rounded-sm" />
                          <div className="h-3 w-3/4 bg-white/6 rounded-sm" />
                        </div>
                      </div>
                    ) : msg.error ? (
                      /* Error state */
                      <div className="bg-rose-500/8 border border-rose-500/25 rounded-sm px-4 py-3 rounded-bl-sm relative overflow-hidden">
                        <div className="absolute top-0 left-0 bottom-0 w-0.5 bg-rose-500" />
                        <div className="flex items-start gap-2">
                          <AlertCircle className="w-3.5 h-3.5 text-rose-400 flex-shrink-0 mt-0.5" />
                          <div className="space-y-2 min-w-0">
                            <p className="text-xs text-white/65 font-sans leading-relaxed">{msg.error}</p>
                            <div className="flex gap-2">
                              <button
                                type="button"
                                onClick={() => handleRetry(msg.query || "")}
                                className="flex items-center gap-1 text-[10px] bg-white/5 hover:bg-white/10 border border-white/10 text-white/60 hover:text-white/80 px-2.5 py-1.5 rounded-sm font-mono uppercase tracking-wider cursor-pointer transition-all"
                              >
                                <RefreshCw className="w-2.5 h-2.5" />
                                Reintentar
                              </button>
                            </div>
                          </div>
                        </div>
                      </div>
                    ) : msg.answer ? (
                      /* Answer content */
                      <div className="bg-black/50 border border-white/5 rounded-sm px-4 py-3 rounded-bl-sm">
                        <div className="flex items-center gap-1.5 mb-2">
                          <Bot className="w-3 h-3 text-emerald-400/60" />
                          <span className="text-[10px] font-mono text-emerald-400/50 uppercase tracking-widest">
                            Análisis
                          </span>
                        </div>

                        <div className="space-y-3">
                          {/* Summary */}
                          <p className="text-xs text-white/80 leading-relaxed font-sans">
                            {msg.answer.summary}
                          </p>

                          {/* Explanation */}
                          {msg.answer.explanation && msg.answer.explanation !== msg.answer.summary && (
                            <p className="text-[11px] text-white/55 leading-relaxed font-sans border-t border-white/5 pt-2">
                              {msg.answer.explanation}
                            </p>
                          )}

                          {/* Viz suggestion — clickable button */}
                          {msg.answer.suggestedVisualization &&
                            msg.answer.suggestedVisualization !== "NONE" &&
                            msg.answer.regions && msg.answer.regions.length > 0 && (
                              <button
                                type="button"
                                onClick={() => {
                                  const zones = [...new Set(msg.answer!.regions!.flatMap((r) => {
                                    const code = normalizeRegionCode(r.regionCode);
                                    return code ? [code] : [];
                                  }))];
                                  onAiAnswer?.(zones);
                                }}
                                className="flex items-center gap-1.5 text-[10px] font-mono text-emerald-400 uppercase tracking-widest bg-emerald-500/8 hover:bg-emerald-500/15 border border-emerald-500/20 hover:border-emerald-500/35 px-3 py-2 rounded-sm transition-all cursor-pointer"
                              >
                                <MapIcon className="w-3 h-3" />
                                Ver en Mapa
                              </button>
                            )}

                          {/* Region buttons */}
                          {msg.answer.regions && msg.answer.regions.length > 0 && (
                            <div className="border-t border-white/5 pt-2">
                              <span className="text-[10px] font-mono text-white/30 uppercase tracking-widest block mb-1.5">
                                Zonas destacadas — clic para ir al mapa
                              </span>
                              <div className="flex flex-wrap gap-1.5">
                                {(showAllRegions[msg.id] ? msg.answer.regions : msg.answer.regions.slice(0, 5)).map((reg) => (
                                  <button
                                    type="button"
                                    key={reg.regionCode}
                                    onClick={() => onNavigateToRegion?.(reg.regionName)}
                                    className="flex items-center gap-1 text-[10px] bg-emerald-500/8 hover:bg-emerald-500/15 text-emerald-300 hover:text-emerald-200 border border-emerald-500/20 hover:border-emerald-500/35 px-2 py-1 rounded-sm font-mono uppercase tracking-wider cursor-pointer transition-all"
                                  >
                                    <MapPin className="w-2.5 h-2.5 flex-shrink-0" />
                                    {reg.regionName}
                                  </button>
                                ))}
                                {!showAllRegions[msg.id] && msg.answer.regions.length > 5 && (
                                  <button
                                    type="button"
                                    onClick={() => setShowAllRegions((prev) => ({ ...prev, [msg.id]: true }))}
                                    className="flex items-center gap-1 text-[10px] bg-white/5 hover:bg-white/10 text-white/50 hover:text-white/70 border border-white/10 hover:border-white/20 px-2 py-1 rounded-sm font-mono uppercase tracking-wider cursor-pointer transition-all"
                                  >
                                    +{msg.answer.regions.length - 5} más
                                  </button>
                                )}
                              </div>
                            </div>
                          )}
                        </div>
                      </div>
                    ) : null}
                  </div>
                )}
              </div>
            ))}
            <div ref={chatEndRef} />
          </div>
        )}
      </div>

      {/* ── Input (always visible at bottom) ───────────────────────────────── */}
      <div className="flex-shrink-0 px-4 py-3 border-t border-white/8 relative z-10">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleQuerySubmit(query);
          }}
          className="relative flex items-center gap-2"
        >
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Ej: ¿Qué zona tiene más brecha digital?"
            aria-label="Pregunta para análisis IA"
            className="flex-1 bg-black/50 border border-white/10 hover:border-white/20 focus:border-emerald-500/40 text-sm text-white px-3.5 py-2.5 rounded-sm focus:outline-none transition-colors font-sans placeholder-white/25 min-w-0"
            id="query-input"
          />
          <button
            type="submit"
            disabled={!query.trim()}
            aria-label="Enviar pregunta"
            id="query-submit"
            className="flex-shrink-0 p-2.5 bg-emerald-500/10 hover:bg-emerald-500/20 disabled:opacity-30 border border-emerald-500/20 hover:border-emerald-500/35 rounded-sm transition-all cursor-pointer"
          >
            <Send className="w-4 h-4 text-emerald-400" />
          </button>
        </form>
        <p className="text-[10px] text-white/20 font-mono mt-2 text-center">
          Presione Enter o haga clic en &#10148; para analizar
        </p>
      </div>
    </div>
  );
}
