# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-11 AS builder

WORKDIR /app

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline \
    -Dmaven.test.skip=true \
    --no-transfer-progress

COPY src src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package \
    -Dmaven.test.skip=true \
    --no-transfer-progress

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=builder --chown=1001:1001 \
    /app/target/*.jar app.jar

USER 1001

EXPOSE 8086

ENTRYPOINT ["java","-jar","app.jar"]
