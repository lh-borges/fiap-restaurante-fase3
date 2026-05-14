# Build
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
COPY shared/pom.xml shared/
COPY usuario-autenticacao/pom.xml usuario-autenticacao/
COPY pagamento/pom.xml pagamento/
COPY restaurante-pedido/pom.xml restaurante-pedido/

RUN mvn dependency:go-offline -pl usuario-autenticacao -am

COPY . .
RUN mvn clean package -pl usuario-autenticacao -am -DskipTests

# Runtime
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S app && adduser -S app -G app

WORKDIR /app

COPY --from=build /app/usuario-autenticacao/target/*.jar app.jar

RUN chown app:app app.jar

USER app

EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]