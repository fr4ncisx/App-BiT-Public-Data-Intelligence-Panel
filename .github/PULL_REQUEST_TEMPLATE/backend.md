# Pull Request Backend

## Tipo de cambio

Indicar el tipo principal de cambio y, si corresponde, el código del backlog relacionado.

Ejemplo: `BE-10`, `BE-14`, `DATA-03`, `CI-01`, `CD-02`, `DOC-03`.

Tipo principal: <!-- feature | fix | refactor | data | api | infra | docs | test -->
Código relacionado: <!-- BE-XX / DATA-XX / CI-XX / CD-XX / DOC-XX -->

## Contexto funcional

Explicar qué necesidad del producto resuelve este cambio.

Debe quedar claro si el PR impacta datos territoriales, ingesta CSV, contratos API, trazabilidad, respuesta IA, mapa, fuentes, errores, seguridad, deploy o documentación.

## Cambios implementados

Describir los cambios por capa o responsabilidad, sin limitarse a una lista de archivos modificados.

Dominio:
<!-- Entidades, Value Objects, enums, reglas de negocio o invariantes modificadas. -->

Application Layer:
<!-- Casos de uso, puertos, orquestación, validaciones de entrada/salida. -->

Adapters / Infrastructure:
<!-- Persistencia, API REST, CSV, IA, UUIDv7, configuración externa, mapper, cliente externo. -->

Base de datos / Flyway:
<!-- Tablas, columnas, constraints, seeds o migraciones impactadas. Indicar si no aplica. -->

## Contratos API afectados

Endpoint(s):
<!-- Ejemplo: POST /api/v1/data/queries, GET /api/v1/maps/regions, GET /api/v1/data/catalog, GET /api/v1/data/sources -->

Request modificado:
<!-- Describir campos agregados, eliminados, renombrados o reglas nuevas. Indicar si no aplica. -->

Response modificado:
<!-- Confirmar si mantiene ApiResponse. Describir data, errors, warnings, sources, confidence, suggestedVisualization. -->

Compatibilidad:
<!-- Indicar si rompe contrato existente, si requiere ajuste frontend o si es compatible. -->

## Datos, fuentes y privacidad

Fuente utilizada:
<!-- antenas_flp.csv | tensor_concentracao.csv | tensor_fluxo_vias.csv | trajetos_comuns.csv | tensor_od.csv | seed social | fuente complementaria | estimación -->

Trazabilidad preservada:
<!-- Explicar cómo se conserva source_id, file_name, source_type, período, confianza o advertencias. -->

Tratamiento de datos:
<!-- Confirmar que el cambio opera con datos agregados y no expone trayectos individuales ni datos sensibles. -->

Archivos grandes:
<!-- Indicar si el cambio evita cargar tensor_mobilidade.csv o tensor_sequencias.csv en requests interactivos. -->

## Arquitectura y diseño técnico

Aislamiento de dominio:
<!-- Confirmar si el dominio permanece sin dependencias de Spring, JPA, MapStruct, uuid-creator u otros frameworks. -->

Uso de MapStruct:
<!-- Indicar mappers modificados o justificar si no aplica. -->

Generación de IDs:
<!-- Confirmar si se usa UUIDv7 desde adapter de infraestructura cuando corresponde. -->

Manejo de errores:
<!-- Describir errores de validación, dominio, fuente no disponible, IA no disponible, CORS, persistencia o ingesta. -->

Observabilidad:
<!-- Logs, métricas, actuator, trazas de ingesta o request_id si aplica. -->

## Evidencia de pruebas

Pruebas ejecutadas:
<!-- Ejemplo: mvn test, prueba manual con Insomnia, health check, seed mínimo, consulta IA, endpoint de mapa. -->

Casos validados:
<!-- Describir escenarios positivos, errores esperados, datos insuficientes, fuente faltante, request inválido, dataset mínimo. -->

Resultado esperado:
<!-- Describir el resultado observable, no solo “pasa”. -->

## Riesgos, límites y decisiones

Riesgo técnico o funcional:
<!-- Rendimiento, memoria, CSV grande, consistencia de datos, contrato API, free tier, dependencia externa, timeout, CORS. -->

Mitigación aplicada:
<!-- Decisión tomada para reducir el riesgo. -->

Límite conocido:
<!-- Algo aceptado para MVP que no bloquea el merge. Indicar si no aplica. -->

## Impacto en documentación o configuración

Variables de entorno:
<!-- Variables nuevas o modificadas. Nunca pegar secretos. -->

README / Swagger / OpenAPI:
<!-- Indicar si se actualizó documentación de ejecución, endpoints, dataset, límites o deploy. -->

Deploy:
<!-- Render, Aiven, Docker, perfil prod, health check. Indicar si no aplica. -->

## Validación mínima antes de merge

- [ ] La aplicación inicia localmente.
- [ ] El cambio no introduce secretos en el repositorio.
- [ ] Las respuestas API mantienen el envelope estándar `ApiResponse` cuando aplica.
- [ ] La trazabilidad de fuentes se conserva cuando el cambio manipula datos.
- [ ] No se exponen trayectos individuales ni datos sensibles.
- [ ] No se cargan CSV grandes completos durante un request interactivo.
