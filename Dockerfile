# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline || true
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:8-jre-jammy
RUN useradd -r -s /bin/false eforms
WORKDIR /app
COPY --from=build /build/target/eforms-dashboard.jar /app/eforms-dashboard.jar
RUN mkdir -p /app/storage/attachments /app/storage/templates /app/storage/import-errors \
    && chown -R eforms:eforms /app
USER eforms
EXPOSE 8080
ENV APP_PROFILE=prod
ENTRYPOINT ["java", "-jar", "/app/eforms-dashboard.jar"]
