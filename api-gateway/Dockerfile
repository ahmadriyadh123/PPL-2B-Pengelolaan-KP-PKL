FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && update-ca-certificates && rm -rf /var/lib/apt/lists/*

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml ./
RUN mvn -s /root/.m2/settings.xml dependency:go-offline -B --no-transfer-progress

COPY src ./src
RUN mvn -s /root/.m2/settings.xml clean package -DskipTests --no-transfer-progress

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
