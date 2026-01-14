FROM eclipse-temurin:21 AS build
WORKDIR /app

# Copy Maven wrapper and settings
COPY .mvn .mvn
COPY mvnw .
COPY settings.xml /root/.m2/settings.xml

# Make Maven wrapper executable
RUN chmod +x mvnw

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B || ./mvnw dependency:resolve -B || true

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -B -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# copy health-wait script and make it executable
COPY wait-for-postgres.sh /usr/local/bin/wait-for-postgres.sh
RUN chmod +x /usr/local/bin/wait-for-postgres.sh

EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/wait-for-postgres.sh"]
