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

COPY --from=build /app/target/notes-api-1.0.0.jar app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh && chown -R spring:spring /app

USER spring:spring

ENV SPRING_PROFILES_ACTIVE=postgres

EXPOSE 8080

# Render sets PORT; entrypoint logs DB env before starting (see deploy logs)
ENTRYPOINT ["/app/docker-entrypoint.sh"]
