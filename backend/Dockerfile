# Build from the repository root so Maven can package ../database/migrations.
FROM docker.io/library/maven:3.9.16-eclipse-temurin-25-noble AS build

WORKDIR /workspace
COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
COPY database/migrations database/migrations

RUN mvn --batch-mode --no-transfer-progress -f backend/pom.xml -DskipTests package \
    && cp backend/target/qr-attendance-backend-0.0.1-SNAPSHOT.jar /workspace/app.jar

FROM docker.io/library/eclipse-temurin:25-jre-noble AS runtime

WORKDIR /app
RUN groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring

COPY --from=build --chown=spring:spring /workspace/app.jar /app/app.jar

USER spring:spring
EXPOSE 8080

# Spring reads Render's PORT from application.yaml (default 8080).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
