# App BiT GeoAnalytics Backend

App BiT addresses a public-decision data gap. The product consolidates
territorial evidence so institutions can identify where training,
employability, mentorship, mental health, and connectivity interventions are
most needed.

The backend for this MVP exposes stable contracts under `/api/v1`, stores raw
source CSV files in Cloudflare R2, persists queryable aggregates in PostgreSQL,
and returns evidence-based responses for maps, sources, and natural language
queries. The system must never expose individual trajectories or personal
mobility sequences.

## Problem

Public and social programs often lack a single, traceable view of territorial
need. Data may exist across mobility datasets, synthetic seeds, and
complementary sources, but decision-makers still need a way to answer questions
such as where connectivity gaps overlap with concentration, where employability
programs are weak, or where remote mental health support is limited by
infrastructure.

App Bit solves that problem by ingesting fixed datasets, pre-aggregating the
results, and exposing them through versioned APIs and a map-oriented product
experience.

## Technology stack

The backend target stack is Java 25, Maven 3.9+, Spring Boot 4.x, Spring MVC,
Spring Data JPA, PostgreSQL, Flyway, Jackson CSV, MapStruct, Spring AI,
OpenAPI/Swagger UI, JUnit 5, Mockito, Docker, Actuator, and an S3-compatible
client for Cloudflare R2.

Cloudflare R2 is the production object storage for fixed source CSV files.
MinIO is only a local S3-compatible replacement for development and tests.
PostgreSQL is the serving layer for catalog, maps, indicators, sources,
ingestion audit, and AI query evidence.

## Dataset and CSV strategy

The main dataset is Vísent CDRView. The MVP does not need every available CSV.
It uses a prioritized subset and defers the highest-volume or highest-risk
files until a controlled batch ingestion flow is ready.

Use this object layout in both Cloudflare R2 and local MinIO:

```text
appbit-datasets/
  raw/
    visent-cdrview/
      antenas_flp.csv
      tensor_concentracao.csv
      tensor_fluxo_vias.csv
      trajetos_comuns.csv
      tensor_od.csv
      tensor_mobilidade.csv
      tensor_sequencias.csv
```

The ingestion catalog defines which fixed files are accepted. Public requests
must never read raw CSV files directly. CSV files must be processed by streaming
and controlled chunks, then persisted as aggregates and indicators in
PostgreSQL.

| File | Role in the MVP |
| --- | --- |
| `antenas_flp.csv` | Required. Source for antennas, coordinates, municipality, and cluster data. |
| `tensor_concentracao.csv` | Required. Source for concentration metrics and heat map data. |
| `tensor_fluxo_vias.csv` | Optional. Source for aggregated corridors and road-flow analysis. |
| `trajetos_comuns.csv` | Optional. Source for k-anonymized common trajectories when available. |
| `tensor_od.csv` | Alternative. Origin-destination matrix by cluster when `trajetos_comuns.csv` is not used. |
| `tensor_mobilidade.csv` | Deferred. High-volume file that must only run through controlled batch ingestion and pre-aggregation. |
| `tensor_sequencias.csv` | Deferred. Sequence-level file excluded from the public MVP because of privacy risk. |

## Complementary sources

The MVP also supports complementary and seed-based sources for social
indicators. These sources can represent training, employability, mentorship,
social experience, and mental health coverage when official territorial sources
are not yet integrated.

Every complementary source exposed by the API must remain traceable. The UI must
make clear whether a value comes from a public source, a seed dataset, a
complementary source, or an estimate.

## Architecture

The backend follows Hexagonal Architecture and Clean Architecture. Domain code
owns business rules and stays independent from frameworks, HTTP contracts,
persistence technologies, AI providers, object storage SDKs, and UUID
libraries.

The intended separation is:

- `domain`: entities, value objects, enums, and domain exceptions.
- `application`: use cases, input ports, output ports, and application DTOs.
- `infrastructure.adapter.in.rest`: controllers, API response envelope,
  request correlation, CORS, OpenAPI, and exception translation.
- `infrastructure.adapter.out.persistence`: JPA entities, repositories, and
  persistence mappers.
- `infrastructure.adapter.out.storage`: S3-compatible adapter for Cloudflare R2
  and local MinIO.
- `infrastructure.identity`: UUIDv7 adapter for infrastructure-generated IDs.

The broader solution separates ingestion, API delivery, AI response generation,
and frontend consumption. The frontend must consume API contracts only and must
never depend on CSV internals.

## Environment variables

Copy `.env.example` to `.env` for local development. Keep real production
secrets in deployment secret storage, not in Git.

The backend uses these variables:

| Variable | Purpose |
| --- | --- |
| `JAVA_TOOL_OPTIONS` | JVM memory and runtime tuning. |
| `SERVER_PORT` | Main HTTP API port. |
| `MANAGEMENT_SERVER_PORT` | Actuator management port. |
| `APPBIT_API_CORS_ALLOWED_ORIGINS` | Allowed frontend origins. |
| `SPRING_AI_GOOGLE_GENAI_API_KEY` | Google GenAI API key for Spring AI. |
| `SPRING_AI_GOOGLE_GENAI_CHAT_MODEL` | Google GenAI chat model. |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL. |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username. |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password. |
| `APPBIT_STORAGE_R2_ENDPOINT` | S3-compatible endpoint for R2 or MinIO. |
| `APPBIT_STORAGE_R2_ACCESS_KEY_ID` | Storage access key. |
| `APPBIT_STORAGE_R2_SECRET_ACCESS_KEY` | Storage secret key. |
| `APPBIT_STORAGE_R2_BUCKET_NAME` | Bucket that stores fixed source CSV files. |
| `APPBIT_STORAGE_R2_REGION` | Storage region. Use `auto` for R2-compatible flows. |
| `APPBIT_STORAGE_R2_CSV_PREFIX` | Prefix for fixed source CSV files. |
| `APPBIT_INGESTION_ENABLED` | Enables operational ingestion flows. |
| `APPBIT_INGESTION_BATCH_SIZE` | Rows processed per ingestion batch. |
| `APPBIT_INGESTION_FAIL_FAST` | Whether ingestion stops at the first invalid row. |

Local MinIO can use `http://localhost:9000` as the storage endpoint.
Production values must point to the Cloudflare R2 endpoint and credentials.

## Local execution

You need Java 25, Docker, and access to a PostgreSQL database. The local compose
file starts MinIO only; it does not start PostgreSQL because developers can use
their own local database.

1. Create your local `.env` from `.env.example`.
2. Ensure PostgreSQL is reachable through `SPRING_DATASOURCE_URL`.
3. Start local object storage:

```powershell
docker compose -f compose.yaml up -d
```

4. Validate the storage configuration:

```powershell
docker compose -f compose.yaml config --quiet
docker compose -f compose.yaml ps
```

5. Run tests:

```powershell
.\mvnw.cmd -q test
```

6. Start the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

When the application is running, the technical health endpoint is:

```text
http://localhost:9090/actuator/health
```

Swagger UI is exposed by SpringDoc in local environments when enabled by the
running profile.

## Endpoints

Public API routes must be versioned, resource-oriented, and exposed under
`/api/v1`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/data/catalog` | Return available regions, indicators, services, and periods. |
| `GET` | `/api/v1/maps/regions` | Return aggregated territorial map data. |
| `POST` | `/api/v1/data/queries` | Answer natural language questions using calculated evidence. |
| `GET` | `/api/v1/data/sources` | Return data sources and ingestion results. |
| `GET` | `/api/v1/maps/flows` | Return aggregated mobility corridors when available. |
| `GET` | `/actuator/health` | Return technical health status. |

`POST /api/v1/data/queries` receives a natural language query plus optional
filters such as `regionCode`, `indicatorType`, and `period`. The response must
include a conclusion, explanation, evidence, involved regions, sources,
warnings, and suggested visualization.

`GET /api/v1/maps/regions` returns aggregated regional data for map rendering,
including coordinates, concentration, connectivity, social indicators, and
warnings when evidence is estimated or incomplete.

## API response model

Every public controller response must use the standard envelope.

Success example:

```json
{
  "success": true,
  "code": "MAP_REGIONS_RETRIEVED",
  "message": "Map regions retrieved successfully.",
  "data": {},
  "meta": {
    "requestId": "018fd74c-9f13-7cc2-b4d8-3fcb0c25d002",
    "timestamp": "2026-06-20T18:30:00Z",
    "apiVersion": "v1"
  },
  "errors": []
}
```

Error example:

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "data": null,
  "meta": {
    "requestId": "018fd74c-9f13-7cc2-b4d8-3fcb0c25d003",
    "timestamp": "2026-06-20T18:30:00Z",
    "apiVersion": "v1"
  },
  "errors": [
    {
      "field": "query",
      "code": "REQUIRED",
      "message": "Query is required."
    }
  ]
}
```

`RequestId`, API version, response timestamps, `ApiResponse`, and
`ApiResponseCode` belong to the REST boundary. They must not be modeled as
domain value objects or used inside business rules.

## Cloudflare R2 and local MinIO

Cloudflare R2 is the production source-file archive. MinIO is the local
replacement for Cloudflare R2 and exists only to support development and tests
without external object-storage calls.

Production storage values follow this shape:

```env
APPBIT_STORAGE_R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
APPBIT_STORAGE_R2_ACCESS_KEY_ID=<r2-access-key-id>
APPBIT_STORAGE_R2_SECRET_ACCESS_KEY=<r2-secret-access-key>
APPBIT_STORAGE_R2_BUCKET_NAME=appbit-datasets
APPBIT_STORAGE_R2_REGION=auto
APPBIT_STORAGE_R2_CSV_PREFIX=raw/visent-cdrview
```

The local compose file uses two services:

- `appbit-minio`: the storage server.
- `appbit-minio-init`: a short-lived initializer that creates the bucket and
  the `raw/visent-cdrview/` prefix.

Upload local CSV files to:

```text
appbit-datasets/raw/visent-cdrview/
```

For CSV files larger than 1 GB, prefer a client upload when browser upload is
unstable. Keep object names unchanged because ingestion rules depend on the
fixed source file names.

## Technical decisions

Large files must be processed by streaming and chunked ingestion. The backend
must never load a full large CSV into memory, and no public endpoint may open a
CSV file from R2 or MinIO during request handling.

Cloudflare R2 stores raw source files. PostgreSQL stores metadata, ingestion
runs, aggregates, indicators, and queryable evidence. AI responses must be
built from prepared evidence generated by application use cases, not by free
access to raw storage or direct unrestricted database exploration.

## Privacy

The MVP exposes aggregated and traceable territorial evidence only. It must not
expose individual trajectories, personal mobility sequences, or inferred
personal behavior.

Mobility and connectivity data must be pre-aggregated before they reach public
API responses. Any optional mobility file used in the MVP must respect the same
aggregation and anonymization boundary.

## Limitations

The MVP does not provide free-form analytics over arbitrary CSV uploads. It only
supports the fixed, cataloged source files defined for the project.

The MVP also does not expose raw mobility sequences, does not serve CSV data
directly from object storage, and may defer some flow-oriented capabilities when
optional files are unavailable or too large for the current ingestion stage.

## Demo queries

Use these queries to demonstrate the intended product behavior:

- `Which regions show high population concentration but weak training coverage?`
- `Where is employability lagging behind territorial potential?`
- `Which areas need better connectivity before remote mental health programs can work?`
- `What sources support the concentration indicators for this region?`

A correct response must include a concise conclusion, supporting evidence,
regions involved, sources, warnings, and a suggested visualization.

## Build and developer checks

Build the application jar with:

```powershell
.\mvnw.cmd -q -DskipTests package
```

Build the runtime image with:

```powershell
docker build -t appbit-geoanalytics-api .
```

Run these checks before handing off backend changes:

```powershell
.\mvnw.cmd -q test
docker compose -f compose.yaml config --quiet
```

For Flyway or SQL changes, validate against PostgreSQL. The default test profile
uses H2 and does not replace PostgreSQL migration testing.
