FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY settings.xml /root/.m2/settings.xml
RUN chmod +x mvnw

COPY pom.xml .
RUN ./mvnw dependency:go-offline -B || true

COPY src ./src
RUN ./mvnw clean package -B -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

COPY wait-for-postgres.sh /usr/local/bin/wait-for-postgres.sh
RUN sed -i 's/\r$//' /usr/local/bin/wait-for-postgres.sh && chmod +x /usr/local/bin/wait-for-postgres.sh

EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/wait-for-postgres.sh"]
