FROM eclipse-temurin:25-jdk-alpine AS deps

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY --chmod=755 mvnw ./
COPY pom.xml ./

RUN ./mvnw \
    --batch-mode \
    --quiet \
    --no-transfer-progress \
    -DskipTests \
    dependency:go-offline

FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace

COPY --from=deps /workspace /workspace
COPY src/ src/

RUN ./mvnw \
    --batch-mode \
    --quiet \
    --no-transfer-progress \
    -DskipTests \
    package \
    && artifact_path="$(find target \
    -maxdepth 1 \
    -type f \
    -name '*.jar' \
    ! -name '*.jar.original' \
    ! -name '*-sources.jar' \
    ! -name '*-javadoc.jar' \
    -print \
    -quit)" \
    && test -n "$artifact_path" \
    && cp "$artifact_path" /workspace/app.jar \
    && test -s /workspace/app.jar

FROM eclipse-temurin:25-jdk-alpine AS jre

COPY --from=build /workspace/app.jar /tmp/app.jar

RUN mkdir -p /tmp/extracted \
    && cd /tmp/extracted \
    && jar xf /tmp/app.jar \
    && jdeps \
    --ignore-missing-deps \
    --multi-release 25 \
    --class-path 'BOOT-INF/lib/*' \
    --print-module-deps \
    /tmp/app.jar > /tmp/modules.txt \
    && jlink \
    --module-path "$JAVA_HOME/jmods" \
    --add-modules $(cat /tmp/modules.txt),java.desktop,jdk.crypto.cryptoki,jdk.crypto.ec,jdk.unsupported,jdk.zipfs,jdk.localedata \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /opt/java/minimal-jre \
    && /opt/java/minimal-jre/bin/java -version

FROM alpine:3.24 AS runtime

ENV APP_HOME=/app \
    JAVA_HOME=/opt/java/minimal-jre \
    PATH="/opt/java/minimal-jre/bin:$PATH" \
    SERVER_PORT=8080 \
    MANAGEMENT_SERVER_PORT=9090 \
    SPRING_PROFILES_ACTIVE=prod \
    TMPDIR=/tmp

LABEL app.name="Panel de Datos Públicos - App BiT" \
    app.description="Responsive B2G web app with AI-assisted public data consolidation." \
    app.version="0.0.1" \
    app.stage="mvp" \
    app.dataset="Visent CDRView" \
    app.source="https://github.com/No-Country-simulation/S06-26-NC-Equipo-70"

WORKDIR /app

RUN apk add --no-cache \
    ca-certificates \
    libstdc++ \
    tini \
    wget \
    && addgroup \
    --system \
    --gid 65532 \
    appgroup \
    && adduser \
    --system \
    --uid 65532 \
    --ingroup appgroup \
    --home /app \
    --shell /sbin/nologin \
    appuser \
    && mkdir -p /tmp \
    --mode=1777 \
    && chown appuser:appgroup /app

COPY --from=jre \
    /opt/java/minimal-jre \
    /opt/java/minimal-jre

COPY --from=build \
    --chown=appuser:appgroup \
    /workspace/app.jar \
    /app/app.jar

USER appuser

EXPOSE 8080 9090

HEALTHCHECK \
    --interval=30s \
    --timeout=5s \
    --start-period=60s \
    --retries=3 \
    CMD wget -q -T 3 -O /dev/null \
    http://127.0.0.1:9090/actuator/health/liveness \
    || exit 1

ENTRYPOINT ["/sbin/tini", "--", "java", "-jar", "/app/app.jar"]