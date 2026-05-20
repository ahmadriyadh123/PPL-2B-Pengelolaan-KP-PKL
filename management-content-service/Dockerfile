# ================== BUILD STAGE ==================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Install certificates
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && \
    update-ca-certificates && rm -rf /var/lib/apt/lists/*

# Copy settings
COPY settings.xml /root/.m2/settings.xml

# Copy pom files
COPY pom.xml ./
COPY */pom.xml ./ 

# Copy source code
COPY src ./src

# Build (dengan retry untuk stabilitas jaringan)
RUN mvn -s /root/.m2/settings.xml clean package -DskipTests --no-transfer-progress \
    -Dmaven.wagon.http.retryhandler.count=3

# ================== RUNTIME STAGE ==================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Salin hasil jar dari build stage
COPY --from=build /app/target/*.jar app.jar

# Gunakan port 8080 (sesuai standar Spring Boot)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]