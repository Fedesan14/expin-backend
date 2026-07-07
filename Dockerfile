# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw && ./mvnw -B -DskipTests dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system expin \
	&& useradd --system --gid expin --home-dir /app --shell /usr/sbin/nologin expin

COPY --from=build /workspace/target/*.jar app.jar

USER expin

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
