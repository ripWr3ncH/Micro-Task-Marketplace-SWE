# syntax=docker/dockerfile:1

FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
