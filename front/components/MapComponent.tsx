"use client";

import React, { useEffect, useRef, useMemo, useEffectEvent } from "react";
import { Region } from "../lib/api";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Escape HTML special characters to prevent XSS when injecting into L.divIcon templates */
function escapeHtml(str: string): string {
  return str
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

/** Tiny circular divIcon for antenna towers */
const getAntennaIcon = (color: string, glow = false) =>
  L.divIcon({
    className: "",
    html: `<div style="
      width: 8px; height: 8px; border-radius: 50%;
      background: ${color};
      border: 1.5px solid rgba(255,255,255,0.6);
      box-shadow: 0 0 ${glow ? "14px 4px" : "6px 1px"} ${color};
    "></div>`,
    iconSize: [8, 8],
    iconAnchor: [4, 4],
  });

/** Large pulsing region beacon for AI highlights */
const getAiBeaconIcon = (color: string) =>
  L.divIcon({
    className: "",
    html: `
      <div style="position:relative;width:40px;height:40px;display:flex;align-items:center;justify-content:center;">
        <div style="
          position:absolute;
          width:40px;height:40px;border-radius:50%;
          background:${color}22;
          border:2px solid ${color};
          animation:ping 1.6s cubic-bezier(0,0,0.2,1) infinite;
        "></div>
        <div style="
          width:14px;height:14px;border-radius:50%;
          background:${color};
          border:2px solid white;
          box-shadow:0 0 16px 4px ${color};
        "></div>
      </div>
      <style>@keyframes ping{0%{transform:scale(1);opacity:1}75%,100%{transform:scale(2.2);opacity:0}}</style>
    `,
    iconSize: [40, 40],
    iconAnchor: [20, 20],
  });

// ── Constants ─────────────────────────────────────────────────────────────────

const REGION_COORDINATES: Record<string, [number, number]> = {
  centro:     [-27.5959, -48.5482],
  continente: [-27.5855, -48.5755],
  norte:      [-27.4520, -48.4510],
  leste:      [-27.6012, -48.4632],
  sul:        [-27.7212, -48.5110],
};

/** Gap color scale */
const gapColor = (score: number) => {
  if (score > 50) return "#f43f5e"; // rose – severe
  if (score > 35) return "#f59e0b"; // amber – moderate
  return "#10b981";                 // emerald – controlled
};

/** Ant towers spread across all five districts */
const ANTENNA_NODES = [
  // Continente
  { lat: -27.5880, lng: -48.5780, region: "continente" },
  { lat: -27.5820, lng: -48.5720, region: "continente" },
  { lat: -27.5920, lng: -48.5700, region: "continente" },
  // Centro
  { lat: -27.5960, lng: -48.5450, region: "centro" },
  { lat: -27.5930, lng: -48.5520, region: "centro" },
  { lat: -27.6010, lng: -48.5400, region: "centro" },
  { lat: -27.5975, lng: -48.5500, region: "centro" },
  // Norte
  { lat: -27.4420, lng: -48.4450, region: "norte" },
  { lat: -27.4580, lng: -48.4560, region: "norte" },
  { lat: -27.4620, lng: -48.4350, region: "norte" },
  { lat: -27.4380, lng: -48.4600, region: "norte" },
  { lat: -27.4700, lng: -48.4480, region: "norte" },
  // Leste
  { lat: -27.5980, lng: -48.4580, region: "leste" },
  { lat: -27.6050, lng: -48.4680, region: "leste" },
  { lat: -27.5900, lng: -48.4700, region: "leste" },
  // Sul
  { lat: -27.7180, lng: -48.5080, region: "sul" },
  { lat: -27.7300, lng: -48.5150, region: "sul" },
  { lat: -27.7050, lng: -48.5200, region: "sul" },
  { lat: -27.7400, lng: -48.5050, region: "sul" },
];

// ── Tooltip html helper ───────────────────────────────────────────────────────
const regionTooltipHtml = (name: string, score: number, color: string, isAiHighlight: boolean) => `
  <div style="
    font-family: monospace;
    font-size: 13px;
    color: #e2e8f0;
    background: #0f172a;
    padding: 8px 12px;
    border: 1px solid ${color};
    border-left: 3px solid ${color};
    border-radius: 2px;
    min-width: 180px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.6);
  ">
    ${isAiHighlight ? `<div style="font-size:10px;color:${color};margin-bottom:4px;letter-spacing:1px;text-transform:uppercase;font-weight:700;">⚡ Región Analizada por IA</div>` : ""}
    <div style="font-weight:700;font-size:14px;margin-bottom:4px;">${name}</div>
    <div style="color:rgba(255,255,255,0.5);font-size:11px;text-transform:uppercase;letter-spacing:1px;">
      Brecha: <span style="color:${color};font-weight:700;">${score.toFixed(1)}%</span>
    </div>
  </div>
`;

// ── Props ─────────────────────────────────────────────────────────────────────
const EMPTY_AI_REGIONS: string[] = [];

interface MapComponentProps {
  regions: Region[];
  selectedRegion: { id: string; name: string } | null;
  activeIndicator: string;
  onSelectRegion: (region: Region) => void;
  aiHighlightedRegions?: string[];
}

// ── Component ─────────────────────────────────────────────────────────────────
export default function MapComponent({
  regions,
  selectedRegion,
  activeIndicator,
  onSelectRegion,
  aiHighlightedRegions = EMPTY_AI_REGIONS,
}: MapComponentProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const overlayLayerRef = useRef<L.LayerGroup | null>(null);
  const heatLayerRef = useRef<L.LayerGroup | null>(null);
  const aiLayerRef = useRef<L.LayerGroup | null>(null);
  const clickHandlersRef = useRef<Array<{ target: L.Layer; type: string; fn: () => void }>>([]);

  const onRegionClick = useEffectEvent((region: Region) => {
    onSelectRegion(region);
  });

  const aiRegionSet = useMemo(() => new Set(aiHighlightedRegions), [aiHighlightedRegions]);

  // ── Init map once ───────────────────────────────────────────────────────────
  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    let timer: ReturnType<typeof setTimeout>;
    try {
      const map = L.map(containerRef.current, {
        center: [-27.5850, -48.5100],
        zoom: 11,
        minZoom: 10,
        maxZoom: 15,
        zoomControl: true,
        attributionControl: true,
      });

      mapRef.current = map;

      // Re-measure after layout settles (dynamic import + grid hydration)
      timer = setTimeout(() => map.invalidateSize(), 300);

      // Basemap: CartoDB Dark Matter — clean, professional, telco-friendly
      L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
        subdomains: "abcd",
        maxZoom: 20,
      }).addTo(map);

      // Add a subtle blue-teal overlay for the Florianópolis bay area
      L.rectangle(
        [[-27.45, -48.62], [-27.78, -48.41]],
        { color: "transparent", fillColor: "#0ea5e9", fillOpacity: 0.03, weight: 0 }
      ).addTo(map);

      // Layer groups for different visual elements
      heatLayerRef.current = L.layerGroup().addTo(map);
      overlayLayerRef.current = L.layerGroup().addTo(map);
      aiLayerRef.current = L.layerGroup().addTo(map);

      return () => {
        clearTimeout(timer);
        map.remove();
        mapRef.current = null;
      };
    } catch {
      // Leaflet init failed — render nothing, parent will show fallback
    }
  }, []);

  // ── Update region overlays whenever data or selection changes ───────────────
  useEffect(() => {
    const map = mapRef.current;
    const heatLayer = heatLayerRef.current;
    const overlayLayer = overlayLayerRef.current;
    if (!map || !heatLayer || !overlayLayer) return;

    heatLayer.clearLayers();
    overlayLayer.clearLayers();

    // Clean up previous subscriptions
    clickHandlersRef.current.forEach(({ target, type, fn }) => { target.off(type, fn); });
    clickHandlersRef.current = [];

    regions.forEach((region) => {
      const coords = REGION_COORDINATES[region.id];
      if (!coords) return;

      const isSelected = selectedRegion?.id === region.id;
      const isAiHighlight = aiRegionSet.has(region.id);
      const color = gapColor(region.gapScore);

      // ── Heat spot (large semi-transparent radial fill) ──
      // Multiple stacked circles simulate a radial heat gradient
      const baseRadius = 1200 + region.gapScore * 30;

      // Outer glow (very transparent)
      L.circle(coords, {
        radius: baseRadius * 1.8,
        fillColor: color,
        fillOpacity: 0.04,
        color: "transparent",
        weight: 0,
      }).addTo(heatLayer);

      // Mid glow
      L.circle(coords, {
        radius: baseRadius * 1.2,
        fillColor: color,
        fillOpacity: 0.08,
        color: "transparent",
        weight: 0,
      }).addTo(heatLayer);

      // Core heat spot
      const coreCircle = L.circle(coords, {
        radius: baseRadius,
        fillColor: color,
        fillOpacity: isSelected ? 0.38 : isAiHighlight ? 0.30 : 0.18,
        color: isSelected ? "#34d399" : isAiHighlight ? color : color,
        weight: isSelected ? 3.5 : isAiHighlight ? 2.5 : 1.5,
        opacity: isSelected ? 1.0 : isAiHighlight ? 0.8 : 0.55,
        dashArray: isAiHighlight && !isSelected ? "8 4" : undefined,
      });

      const clickHandler = () => onRegionClick(region);
      coreCircle.on("click", clickHandler);
      clickHandlersRef.current.push({ target: coreCircle, type: "click", fn: clickHandler });
      coreCircle.bindTooltip(
        regionTooltipHtml(escapeHtml(region.name || region.regionName || "Zona"), region.gapScore, color, isAiHighlight),
        { permanent: false, direction: "top", opacity: 1, className: "leaflet-tooltip-custom" }
      );
      coreCircle.addTo(overlayLayer);

      // Region label marker (invisible icon, just for the label)
      const labelIcon = L.divIcon({
        className: "",
        html: `
          <div style="
            font-family: monospace;
            font-size: ${isSelected ? "13px" : "11px"};
            font-weight: 700;
            color: ${isSelected ? "#34d399" : isAiHighlight ? color : "rgba(255,255,255,0.8)"};
            text-transform: uppercase;
            letter-spacing: 1.5px;
            white-space: nowrap;
            text-shadow: 0 1px 4px rgba(0,0,0,0.9), 0 0 8px rgba(0,0,0,0.8);
            pointer-events: none;
          ">
            ${escapeHtml((region.name || region.regionName || "Zona").split(" ").slice(0, 2).join(" "))}
            ${isAiHighlight ? "<span style='color:" + color + ";font-size:9px;display:block;margin-top:1px;'>⚡ IA</span>" : ""}
          </div>`,
        iconAnchor: [0, 0],
      });

      L.marker(coords, { icon: labelIcon, interactive: false }).addTo(overlayLayer);
    });

    // Antenna tower nodes
    ANTENNA_NODES.forEach((node) => {
      const regionData = regions.find((r) => r.id === node.region);
      const isAiRegion = aiRegionSet.has(node.region);
      const color = regionData ? gapColor(regionData.gapScore) : "#10b981";

      const marker = L.marker([node.lat, node.lng], {
        icon: getAntennaIcon(color, isAiRegion),
      });

      marker.bindTooltip(
        `<div style="font-family:monospace;font-size:11px;color:#94a3b8;background:#0f172a;padding:4px 8px;border:1px solid rgba(255,255,255,0.1);border-radius:2px;">
          Antena CDR${isAiRegion ? " • <span style='color:" + color + "'>Zona Analizada</span>" : ""}
        </div>`,
        { className: "leaflet-tooltip-custom" }
      );

      marker.addTo(overlayLayer);
    });

    return () => {
      clickHandlersRef.current.forEach(({ target, type, fn }) => { target.off(type, fn); });
      clickHandlersRef.current = [];
      heatLayer.clearLayers();
      overlayLayer.clearLayers();
    };
  }, [regions, selectedRegion, activeIndicator, aiRegionSet]);

  // ── AI highlight beacon layer (pulsing markers on ai regions) ───────────────
  useEffect(() => {
    const aiLayer = aiLayerRef.current;
    if (!aiLayer) return;

    aiLayer.clearLayers();

    if (aiHighlightedRegions.length === 0) return;

    aiHighlightedRegions.forEach((regionId) => {
      const coords = REGION_COORDINATES[regionId];
      const regionData = regions.find((r) => r.id === regionId);
      if (!coords) return;

      const color = regionData ? gapColor(regionData.gapScore) : "#f59e0b";

      const beacon = L.marker(coords, {
        icon: getAiBeaconIcon(color),
        zIndexOffset: 1000,
      });

      beacon.addTo(aiLayer);
    });
  }, [aiHighlightedRegions, regions]);

  return (
    <div
      ref={containerRef}
      className="w-full h-full min-h-[380px] bg-[#060a12] rounded-sm overflow-hidden shadow-inner"
      style={{ zIndex: 1 }}
    />
  );
}
