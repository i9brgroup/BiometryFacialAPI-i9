# Multi-stage Dockerfile for building and running the Spring Boot API

# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only the pom first to leverage Docker layer caching for dependencies
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy the rest of the source and build
COPY src ./src
RUN mvn -q -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the fat jar built by Spring Boot
COPY --from=build /app/target/facial-auth-i9-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
