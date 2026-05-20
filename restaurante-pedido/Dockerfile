# syntax=docker/dockerfile:1

# Build
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY . .

# O cache mount do BuildKit mantém o repositório local Maven (~/.m2) entre
# builds. Downloads bem-sucedidos persistem, então rede instável só faz a
# build avançar incrementalmente em vez de recomeçar do zero.
# As flags de retry retentam downloads interrompidos (inclusive os que já
# enviaram parte do corpo — requestSentEnabled).
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -Dmaven.wagon.http.retryHandler.count=5 \
        -Dmaven.wagon.http.retryHandler.requestSentEnabled=true \
        clean package -pl restaurante-pedido -am -DskipTests

# Runtime
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=build /app/restaurante-pedido/target/*.jar app.jar

RUN chown app:app app.jar

USER app

EXPOSE 8082

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
