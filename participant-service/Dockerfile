# ================== BUILD STAGE ==================
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Update CA Certificates (untuk menghindari SSL issue)
RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates

# Copy Maven settings (mirror)
COPY settings.xml /root/.m2/settings.xml

# Copy pom.xml untuk dependency caching
COPY pom.xml .
# Jika multi-module, copy semua pom
COPY */pom.xml ./

RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source code
COPY . .

# Build
RUN mvn clean package -DskipTests --no-transfer-progress \
    -Dmaven.wagon.http.retryhandler.count=3

# ================== RUNTIME STAGE ==================
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]