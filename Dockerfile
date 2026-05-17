# Build stage — single Maven run (go-offline often breaks Render Docker builds)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

ENV MAVEN_OPTS="-Xmx768m"

COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Runtime stage — Debian-based JRE (fewer Alpine/JDBC issues than musl)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=build /app/target/notes-api-1.0.0.jar app.jar

ENV SPRING_PROFILES_ACTIVE=postgres

EXPOSE 8080

# Render sets PORT; Spring reads server.port=${PORT}
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
