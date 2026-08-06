# AppBiT — Frontend

_Panel de inteligencia territorial: datos públicos, preguntas en lenguaje natural, decisiones con evidencia._

[![Sitio en vivo](https://img.shields.io/badge/web-geoanalytics--dqf.pages.dev-1f6feb)](https://geoanalytics-dqf.pages.dev)
[![Next.js](https://img.shields.io/badge/Next.js-000000?logo=nextdotjs&logoColor=fff)](https://nextjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=fff)](https://www.typescriptlang.org)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?logo=tailwindcss&logoColor=fff)](https://tailwindcss.com)
[![Playwright](https://img.shields.io/badge/Playwright-2EAD33?logo=playwright&logoColor=fff)](https://playwright.dev)
[![pnpm](https://img.shields.io/badge/pnpm-F69220?logo=pnpm&logoColor=fff)](https://pnpm.io)

---

## Funcionalidades

- **Mapa interactivo** — datos agregados por región y municipio, sin exponer trayectorias individuales.
- **Indicadores y brechas** — formación, empleabilidad, mentoría y salud mental a nivel territorial.
- **Detalle regional** — panel con el contexto completo de cada territorial.
- **Chat de IA** — consultas en lenguaje natural, respuestas con evidencia del dataset y sugerencia de visualización.
- **Ingesta trazable** — estado de carga de datasets públicos con registro de fuentes.

---

## Empezando

### Requisitos

- Node.js ≥ 20
- pnpm

### Configuración

```bash
cp .env.example .env.local
```

| Variable | Descripción | Default |
|---|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | URL base de la API del backend | `http://localhost:8080` |

Ajustá `NEXT_PUBLIC_API_BASE_URL` si el backend corre en otro host o puerto.

> La clave de API de Gemini se configura en el **backend**, no en el frontend.

### Ejecución local

```bash
pnpm install
pnpm dev
```

La app corre por defecto en `http://localhost:3000` (configurable: `pnpm dev -p <puerto>`).

---

## Scripts

| Comando | Descripción |
|---|---|
| `pnpm dev` | Servidor de desarrollo en `http://localhost:3000` |
| `pnpm build` | Compilación de producción |
| `pnpm lint` | ESLint |
| `pnpm test:all` | E2E con Playwright (requiere backend levantado y datos disponibles) |

---

Este panel consume la API de [AppBiT backend](../back/README.md).