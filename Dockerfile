# syntax=docker/dockerfile:1.7
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

# Recibe el nombre del módulo desde docker-compose.yml
ARG MODULE

# Copia el proyecto Maven completo
COPY . .

# Compila únicamente el módulo solicitado.
# -am también construye cualquier módulo del cual dependa.
# el caché evita descargar Maven nuevamente en cada servicio.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B \
        -pl "${MODULE}" \
        -am \
        clean package \
        -DskipTests \
    && mkdir -p /out \
    && JAR_FILE="$(find "${MODULE}/target" \
        -maxdepth 1 \
        -type f \
        -name '*.jar' \
        ! -name '*-sources.jar' \
        ! -name '*-javadoc.jar' \
        | head -n 1)" \
    && test -n "${JAR_FILE}" \
    && cp "${JAR_FILE}" /out/app.jar

# ETAPA 2: EJECUCIÓN
FROM eclipse-temurin:17-jre-jammy

# Curl permite comprobar /actuator/health
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /out/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]