# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-26@sha256:b2c1ad85954592f9928e84327c65201f308ad9b5d8ed7d5b823717c97bf23fbb AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -B -Dstyle.color=never

FROM eclipse-temurin:17-jre-ubi10-minimal@sha256:f5d48f4d9d15ef5e60ebd12232698ad1f152bb24e2daa7453e4ff0de1341da8d

WORKDIR /app

COPY --from=build --chown=10001:10001 /app/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=5 \
  CMD wget --quiet --spider --header='X-Forwarded-Proto: https' http://localhost:8081/actuator/health/readiness || exit 1

USER 10001:10001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
