# syntax=docker/dockerfile:1
#
# Build em 4 estagios (ver usuario-autenticacao/Dockerfile para detalhes):
#  1) build      -> fat jar
#  2) extract    -> layers Spring Boot
#  3) cds        -> training run (profile cds, H2 in-memory, Kafka listener off)
#  4) runtime    -> layers separadas + AppCDS archive

ARG JDK_IMAGE=eclipse-temurin:25-jre-alpine

# ============ 1) Build ============
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -Dmaven.wagon.http.retryHandler.count=5 \
        -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
        clean package -pl restaurante-pedido -am -DskipTests

# ============ 2) Extract layers ============
FROM ${JDK_IMAGE} AS extract
WORKDIR /build
COPY --from=build /app/restaurante-pedido/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ============ 3) CDS training ============
FROM ${JDK_IMAGE} AS cds
WORKDIR /app
COPY --from=extract /build/dependencies/ ./
COPY --from=extract /build/spring-boot-loader/ ./
COPY --from=extract /build/snapshot-dependencies/ ./
COPY --from=extract /build/application/ ./
RUN java -XX:ArchiveClassesAtExit=/app/application.jsa \
         -Dspring.context.exit=onRefresh \
         -Dspring.profiles.active=cds \
         org.springframework.boot.loader.launch.JarLauncher

# ============ 4) Runtime ============
FROM ${JDK_IMAGE}
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=extract --chown=app:app /build/dependencies/ ./
COPY --from=extract --chown=app:app /build/spring-boot-loader/ ./
COPY --from=extract --chown=app:app /build/snapshot-dependencies/ ./
COPY --from=extract --chown=app:app /build/application/ ./
COPY --from=cds     --chown=app:app /app/application.jsa ./

USER app
EXPOSE 8082

ENTRYPOINT ["java", \
    "-XX:SharedArchiveFile=application.jsa", \
    "org.springframework.boot.loader.launch.JarLauncher"]
