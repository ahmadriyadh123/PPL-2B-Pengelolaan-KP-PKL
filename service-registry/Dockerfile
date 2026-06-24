# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline \
    -DskipTests \
    --no-transfer-progress

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package \
    -DskipTests \
    --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder --chown=1001:1001 \
    /app/target/*.jar app.jar

USER 1001

EXPOSE 8761

ENTRYPOINT ["java","-jar","app.jar"]