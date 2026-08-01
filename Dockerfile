# syntax=docker/dockerfile:1.7

FROM maven:3.9.11-eclipse-temurin-17@sha256:e4a7ace3dc0d645ed97f8d9ad0b0d3f0b14fa8d150138f27f116d7105a639b82 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -B -Dstyle.color=never

FROM eclipse-temurin:24-jre-ubi10-minimal@sha256:3682dc9d9f189459f87975d11ac5cca3975d5ad7fbe311ad6b4cfad39444e07f

WORKDIR /app

COPY --from=build --chown=10001:10001 /app/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=5 \
  CMD wget --quiet --spider --header='X-Forwarded-Proto: https' http://localhost:8081/actuator/health/readiness || exit 1

USER 10001:10001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
