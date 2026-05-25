# syntax=docker/dockerfile:1
#
# Build em 4 estagios:
#  1) build      -> compila o jar fat tradicional (com BuildKit cache em ~/.m2)
#  2) extract    -> separa o jar em layers Spring Boot (deps / loader / app)
#  3) cds        -> training run com profile `cds` (H2 in-memory, sem Kafka/MySQL)
#                   gera application.jsa (AppCDS archive)
#  4) runtime    -> copia as layers + archive; layers separadas = cache de docker
#                   mais eficiente quando so o codigo da app muda
#
# Ganhos vs versao anterior:
#  - Rebuild incremental: codigo novo invalida so a layer `application`,
#    nao a `dependencies` (~50MB de jars Maven nao sao reenviados)
#  - Boot ~30% mais rapido via AppCDS (classes pre-carregadas no archive)

ARG JDK_IMAGE=eclipse-temurin:25-jre-alpine

# ============ 1) Build ============
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -Dmaven.wagon.http.retryHandler.count=5 \
        -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
        clean package -pl usuario-autenticacao -am -DskipTests

# ============ 2) Extract layers ============
FROM ${JDK_IMAGE} AS extract
WORKDIR /build
COPY --from=build /app/usuario-autenticacao/target/*.jar app.jar
# Spring Boot 4 mantem compat com `layertools` (estavel desde 2.4)
RUN java -Djarmode=layertools -jar app.jar extract

# ============ 3) CDS training ============
FROM ${JDK_IMAGE} AS cds
WORKDIR /app
COPY --from=extract /build/dependencies/ ./
COPY --from=extract /build/spring-boot-loader/ ./
COPY --from=extract /build/snapshot-dependencies/ ./
COPY --from=extract /build/application/ ./
# `spring.context.exit=onRefresh` faz o app encerrar com codigo 0 logo apos
# o ApplicationContext ser refrescado. -XX:ArchiveClassesAtExit grava o jsa.
RUN java -XX:ArchiveClassesAtExit=/app/application.jsa \
         -Dspring.context.exit=onRefresh \
         -Dspring.profiles.active=cds \
         org.springframework.boot.loader.launch.JarLauncher

# ============ 4) Runtime ============
FROM ${JDK_IMAGE}
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Ordem importa: layers menos voláteis primeiro para max cache.
COPY --from=extract --chown=app:app /build/dependencies/ ./
COPY --from=extract --chown=app:app /build/spring-boot-loader/ ./
COPY --from=extract --chown=app:app /build/snapshot-dependencies/ ./
COPY --from=extract --chown=app:app /build/application/ ./
COPY --from=cds     --chown=app:app /app/application.jsa ./

USER app
EXPOSE 8081

ENTRYPOINT ["java", \
    "-XX:SharedArchiveFile=application.jsa", \
    "org.springframework.boot.loader.launch.JarLauncher"]
