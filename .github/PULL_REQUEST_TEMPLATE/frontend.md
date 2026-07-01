# Pull Request Frontend

## Tipo de cambio

Indicar el tipo principal de cambio y, si corresponde, el código del backlog relacionado.

Ejemplo: `FE-04`, `FE-10`, `FE-16`, `FE-20`, `FE-23`, `CD-06`, `DOC-04`.

Tipo principal: <!-- feature | fix | ui | map | data | refactor | deploy | docs | test -->
Código relacionado: <!-- FE-XX / CD-XX / DOC-XX / QA-XX -->

## Contexto funcional

Explicar qué flujo mejora o corrige este cambio.

Debe quedar claro si el PR impacta pantalla de consulta, mapa territorial, filtros, panel lateral, leyenda, fuentes, advertencias, estados vacíos, manejo de errores, responsividad o integración con backend.

## Cambios implementados

Pantalla o feature:
<!-- Consulta IA, mapa, catálogo, fuentes, filtros, panel lateral, layout, navegación, estados UI. -->

Componentes:
<!-- Componentes agregados o modificados: FilterPanel, MetricCard, SourceBadge, EmptyState, LoadingState, ErrorState, etc. -->

Estado y datos:
<!-- TanStack Query, cache, invalidación, loading, error, empty, tipado TypeScript, normalización de response. -->

Visualización:
<!-- Recharts, Leaflet, MapLibre, marcadores, capas, leyenda, ranking, flujos, colores semánticos. -->

## Integración API

Endpoint(s) consumidos:
<!-- Ejemplo: POST /api/v1/data/queries, GET /api/v1/maps/regions, GET /api/v1/data/catalog, GET /api/v1/data/sources -->

Uso de `NEXT_PUBLIC_API_BASE_URL`:
<!-- Confirmar si el cambio respeta la variable de entorno y no hardcodea la URL del backend. -->

Envelope `ApiResponse`:
<!-- Explicar cómo se interpreta success, status, code, message, data, errors o warnings. -->

Compatibilidad con backend:
<!-- Indicar si requiere PR backend relacionado, mock temporal, contrato confirmado o ajuste pendiente. -->

## Experiencia de usuario

Usuario objetivo:
<!-- Gestor público, analista de políticas sociales, investigador u organismo gubernamental. -->

Resultado esperado:
<!-- Describir qué puede entender, consultar o decidir el usuario luego del cambio. -->

Claridad funcional:
<!-- Explicar cómo se muestran fuentes, límites, advertencias, confianza o significado de métricas. -->

Estados cubiertos:
<!-- Loading, error, empty, sin evidencia suficiente, datos incompletos, fuente sintética o estimada. -->

## Responsividad y accesibilidad

Desktop:
<!-- Comportamiento esperado. -->

Tablet:
<!-- Comportamiento esperado. -->

Mobile:
<!-- Comportamiento esperado. -->

Accesibilidad básica:
<!-- Labels, foco, contraste, navegación por teclado, aria-label cuando aplique. -->

## Evidencia de pruebas

Pruebas ejecutadas:
<!-- Ejemplo: pnpm lint, pnpm build, prueba manual, flujo consulta, mapa, mobile viewport. -->

Casos validados:
<!-- Selección de región, selección de indicador, consulta con evidencia, consulta sin evidencia, error API, loading, responsive. -->

Evidencia visual:
<!-- Adjuntar captura o video cuando el cambio sea visual. -->

## Riesgos, límites y decisiones

Riesgo funcional o visual:
<!-- Contrato API incompleto, mapa pesado, renderizado en mobile, inconsistencias de datos, CORS, URL productiva. -->

Mitigación aplicada:
<!-- Decisión tomada para reducir el riesgo. -->

Límite conocido:
<!-- Algo aceptado para MVP que no bloquea el merge. Indicar si no aplica. -->

## Impacto en documentación o configuración

Variables de entorno:
<!-- Variables nuevas o modificadas. Nunca pegar secretos. -->

README / guía de demo:
<!-- Indicar si se actualizó documentación de ejecución, uso, limitaciones o flujo de demo. -->

Deploy:
<!-- Cloudflare Pages, build, variables públicas, consumo de API productiva. Indicar si no aplica. -->

## Validación mínima antes de merge

- [ ] El frontend compila localmente.
- [ ] No se hardcodea la URL del backend.
- [ ] El cliente HTTP interpreta correctamente `ApiResponse`.
- [ ] La UI muestra fuentes, límites o advertencias cuando aplica.
- [ ] El flujo principal funciona en escritorio y celular.
- [ ] No se muestran datos individuales ni trayectos identificables.
