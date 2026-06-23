# Stage 1: build the Spring Boot jar using Gradle
FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /workspace

# Copy Gradle project
COPY . .

# Build the Spring Boot application
RUN gradle clean bootJar --no-daemon

# Stage 2: runtime image with just the JRE and the built jar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /workspace/build/libs/*.jar app.jar

# Render sets $PORT; Spring Boot must bind to it
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]