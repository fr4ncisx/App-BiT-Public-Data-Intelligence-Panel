# AppBiT

**Panel de inteligencia territorial pública.** Convierte datos de movilidad y conectividad en evidencia accionable para la decisión pública: cobertura de red, brechas socio-digitales, empleabilidad, formación y salud mental, consultables en lenguaje natural.

[![Sitio en vivo](https://img.shields.io/badge/web-geoanalytics--dqf.pages.dev-1f6feb)](https://geoanalytics-dqf.pages.dev)
[![CI Backend](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel/actions/workflows/back-ci-cd.yml/badge.svg)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel/actions/workflows/back-ci-cd.yml)
[![CI Storage](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel/actions/workflows/back-storage-tests.yml/badge.svg)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel/actions/workflows/back-storage-tests.yml)
[![CI Frontend](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel/actions/workflows/front-ci-cd.yml/badge.svg)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel/actions/workflows/front-ci-cd.yml)

[![Java](https://img.shields.io/badge/Java-25-E76F00?logo=openjdk&logoColor=fff)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?logo=spring&logoColor=fff)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-000000?logo=nextdotjs&logoColor=fff)](https://nextjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=fff)](https://www.typescriptlang.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=fff)](https://www.postgresql.org)
[![Gemini](https://img.shields.io/badge/Gemini-8E75B2?logo=googlegemini&logoColor=fff)](https://gemini.google.com)
[![Backblaze B2](https://img.shields.io/badge/Backblaze_B2-E02D5B?logo=backblaze&logoColor=fff)](https://www.backblaze.com/cloud-storage)
[![MinIO](https://img.shields.io/badge/MinIO-C72E49?logo=minio&logoColor=fff)](https://min.io)
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=fff)](https://www.docker.com)

---

## El problema

Los datos oficiales sobre inclusión digital y social existen, pero están dispersos, sin normalizar y sin trazabilidad. Quien decide políticas públicas no tiene una vista consolidada de la necesidad territorial — ni la capacidad de preguntársela directamente a los datos.

| Problema | Impacto |
|---|---|
| Datos públicos dispersos | Imposible construir una visión regional integrada. |
| Sin visualización territorial | Las desigualdades por zona o municipio pasan desapercibidas. |
| Baja accesibilidad técnica | Los usuarios no técnicos no pueden consultar datos complejos. |
| Decisiones sin evidencia | La gestión se apoya en percepción, no en datos trazables. |
| Brechas de conectividad invisibles | Los programas sociales remotos fracasan sin infraestructura digital. |

---

## Cómo lo resuelve

AppBiT ofrece dos formas de análisis complementarias, sobre el dataset público Vísent CDRView:

1. **Exploración visual**: un mapa territorial interactivo con datos agregados por región, cluster y municipio.
2. **Consulta asistida**: preguntas en lenguaje natural con respuestas estructuradas, basadas exclusivamente en la evidencia disponible.

Preguntas reales que el agente responde con datos:

> - ¿Dónde faltan programas de formación para jóvenes de bajos ingresos?
> - ¿Qué regiones tienen alta concentración de personas y baja empleabilidad?
> - ¿Dónde hay baja conectividad y necesidad de salud mental remota?
> - ¿Qué zonas deberían priorizarse para mentorías públicas?
> - ¿Dónde existen experiencias sociales que podrían replicarse?

---

## Funcionalidades

[![Mapa](https://img.shields.io/badge/Mapa-1f6feb)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![Indicadores](https://img.shields.io/badge/Indicadores-6f42c1)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![Ranking](https://img.shields.io/badge/Ranking-0d6efd)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![IA](https://img.shields.io/badge/IA-8E75B2)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![Ingesta](https://img.shields.io/badge/Ingesta-198754)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![Privacidad](https://img.shields.io/badge/Privacidad-212529)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)

- **Mapa interactivo** — datos agregados por región, cluster y municipio, sin exponer trayectorias individuales.
- **Indicadores y brechas socio-digitales** — formación, empleabilidad, mentoría y salud mental a nivel territorial.
- **Ranking regional** — priorización de intervenciones por indicador, con evidencia.
- **Chat de IA** — interpreta la pregunta, la contrasta contra la evidencia preparada y sugiere la visualización adecuada.
- **Ingesta trazable** — procesamiento en streaming, agregación previa y registro de fuentes.
- **Privacidad por diseño** — solo datos agregados; diferenciación explícita de datos disponibles, sintéticos y complementarios.

---

## Qué devuelve la IA

Cada respuesta del agente es una **conclusión estructurada**, no texto libre:

| Elemento | Descripción |
|---|---|
| Resumen | Conclusión principal en lenguaje claro, para perfiles no técnicos. |
| Datos | Valores utilizados para sostener la conclusión. |
| Regiones | Zonas involucradas en el hallazgo. |
| Fuentes | Archivos o datasets que respaldan cada dato. |
| Visualización sugerida | Mapa, tabla, ranking o comparación según el resultado. |
| Advertencias | Datos faltantes, sintéticos o con baja confianza. |

Regla de oro: **el agente nunca inventa datos** — si la evidencia es insuficiente, lo dice explícitamente.

---

## Los datos

[![Dataset sintético](https://img.shields.io/badge/dataset-V%C3%ADsent_CDRView-198754)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![LGPD](https://img.shields.io/badge/privacidad-k--anonimato_K%3D5_LGPD-6f42c1)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)

El MVP se construye sobre el dataset público Vísent CDRView, de la Región Metropolitana de Florianópolis (Brasil):

- **200.000 suscriptores sintéticos** y 15 días de movilidad urbana modelada.
- **132 estaciones radio base reales** (catálogo público Anatel) geocodificadas.
- **27 clusters geográficos** calibrados por densidad poblacional (IBGE 2022).
- **k-anonimato** aplicado (K=3 en el dataset; K=5 exigido en producción según LGPD).
- Tensores de **hasta 2,7 GB procesados en streaming por chunks**, nunca completos en memoria.

> Este dataset es **ficticio**: no contiene datos reales de suscriptores ni de operadoras. El producto está diseñado para operar igual con datos reales anonimizados.

---

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 25 · Spring Boot 4.1 · Spring Data JPA · REST `/api/v1` |
| Frontend | Next.js (App Router) · TypeScript · Tailwind CSS |
| Base de datos | PostgreSQL · Flyway · Jackson 3 |
| IA | Spring AI 2.0 (Gemini) |
| Almacenamiento | Backblaze B2 (producción) · MinIO (desarrollo) |
| Infraestructura | Docker · GitHub Actions |
| Calidad | JUnit + Mockito · Playwright (e2e) |

[![Flyway](https://img.shields.io/badge/Flyway-CC0200?logo=flyway&logoColor=fff)](https://flywaydb.org)
[![Tailwind](https://img.shields.io/badge/Tailwind_CSS-06B6D4?logo=tailwindcss&logoColor=fff)](https://tailwindcss.com)
[![Playwright](https://img.shields.io/badge/Playwright-2EAD33?logo=playwright&logoColor=fff)](https://playwright.dev)
[![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?logo=githubactions&logoColor=fff)](https://github.com/features/actions)

---

## Arquitectura

Monorepo con frontend y backend independientes. El backend sigue **arquitectura hexagonal**: el dominio está aislado de los frameworks y la API se expone vía REST versionado (`/api/v1`). La ingesta de datasets procesa en **streaming con agregación previa** — nunca se cargan datasets completos en memoria de un request. Los archivos fuente viven en **Backblaze B2**; **PostgreSQL** sirve la capa de consulta; **Spring AI** media la conversación con el modelo de lenguaje, siempre sobre evidencia preparada.

---

## Seguridad e IA responsable

[![CVE patched](https://img.shields.io/badge/seguridad-CVEs_parcheados-brightgreen)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![Anti-alucinación](https://img.shields.io/badge/IA-anti--alucinaci%C3%B3n-8E75B2)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![Sanitización](https://img.shields.io/badge/IA-sanitizaci%C3%B3n_de_salida-8E75B2)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![Agregación](https://img.shields.io/badge/datos-agregados_sin_trayectorias-198754)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)

- **Evidencia antes que opinión** — el modelo solo responde con datos preparados del dataset, reduciendo alucinaciones.
- **Salidas sanitizadas** y límites estrictos en todas las entradas de la API.
- **Privacidad por diseño** — únicamente datos agregados, sin trayectorias individuales ni inferencias personales.
- **Dependencias bajo monitoreo de CVEs** y actualizadas a versiones parcheadas.
- **Trazabilidad de requests** — correlación por `X-Request-Id` en cada interacción.

---

## API en un vistazo

[![REST](https://img.shields.io/badge/REST-%2Fapi%2Fv1-6f42c1)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)

| Endpoint | Método | Uso |
|---|---|---|
| `/api/v1/data/queries` | POST | Consulta en lenguaje natural con filtros opcionales. |
| `/api/v1/maps/regions` | GET | Datos agregados para el mapa territorial. |
| `/api/v1/data/regions/{regionCode}/indicators` | GET | Indicadores de una región específica. |
| `/api/v1/data/sources` | GET | Fuentes disponibles y estado de ingesta. |

Todas las respuestas usan un **envelope estándar** (`success`, `status`, `code`, `data`, `meta`, `errors`) y códigos de error estables: `VALIDATION_ERROR`, `DATA_NOT_FOUND`, `INSUFFICIENT_EVIDENCE`, `DATA_SOURCE_UNAVAILABLE`, `INTERNAL_ERROR`.

---

## Puesta en marcha

```bash
# 1. Infraestructura local (MinIO + PostgreSQL)
docker compose -f back/compose.yaml up -d

# 2. Backend (perfil dev)
cd back && ./mvnw spring-boot:run

# 3. Frontend
cd front && pnpm install && pnpm dev

# 4. Salud del backend
curl http://localhost:9090/actuator/health
```

Puertos por defecto: **8080** (API), **9090** (health/actuator) y **3000** (frontend). Todos son configurables: el backend vía `SERVER_PORT` y `MANAGEMENT_SERVER_PORT` (variables de entorno o `.env`); el frontend vía `pnpm dev -p <puerto>`. La URL del backend se define en el frontend con `NEXT_PUBLIC_API_BASE_URL`.

---

## Calidad

[![Tests](https://img.shields.io/badge/tests-485%20%C2%B7%200%20fallos-25A162)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel)
[![Build](https://img.shields.io/badge/build-pasando-brightgreen)](https://github.com/fr4ncisx/App-BiT-Public-Data-Intelligence-Panel/actions)
[![E2E](https://img.shields.io/badge/e2e-Playwright-2EAD33)](https://playwright.dev)

- **485 tests automatizados** en backend (JUnit + Mockito), cubriendo dominio, API, ingesta e integración con almacenamiento.
- **3 pipelines de CI** en GitHub Actions: build y test del backend, tests de integración con almacenamiento y build + e2e del frontend.
- **Playwright** para pruebas e2e del panel, **ESLint** y **Spotless** para consistencia del código.

---

## Estado y visión

[![Estado](https://img.shields.io/badge/estado-MVP%20en%20vivo-brightgreen)](https://geoanalytics-dqf.pages.dev)

El MVP está funcional y en línea. La visión de producto:

- Incorporar **más fuentes públicas** y ampliar el catálogo de indicadores.
- Capa de **flujos y corredores** urbanos sobre el mapa.
- **Dashboards exportables** para la gestión institucional.
- Alertas por umbral configurable y comparativos regionales.

---

## Repositorio

- [Backend](back/README.md) — API, ingesta e IA.
- [Frontend](front/README.md) — panel web.
