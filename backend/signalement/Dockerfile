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

# Copy wait-for-postgres.sh and fix Windows line endings
COPY wait-for-postgres.sh /tmp/wait.sh
RUN sed -i 's/\r$//' /tmp/wait.sh && \
    mv /tmp/wait.sh /usr/local/bin/wait-for-postgres.sh && \
    chmod +x /usr/local/bin/wait-for-postgres.sh

EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/wait-for-postgres.sh"]
CMD ["java", "-jar", "app.jar"]