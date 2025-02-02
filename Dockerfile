FROM maven:3.9.9-amazoncorretto-17 AS build

WORKDIR /service

COPY pom.xml .
COPY src/ ./src/

RUN mvn clean package

FROM openjdk:17-jdk-slim

WORKDIR /service

COPY --from=build /service/target/monitor-1.0-SNAPSHOT.jar /service/monitor-1.0-SNAPSHOT.jar
