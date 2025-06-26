FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy Maven descriptor first and resolve dependencies (leverages Docker layer caching)
COPY pom.xml ./
RUN mvn -q dependency:go-offline

# Copy application source and build the fat-jar, skipping tests for speed
COPY src ./src
RUN mvn -q package -DskipTests

# ----------------------------------------------------------------------
# ── Runtime image ─────────────────────────────────────────────────────
# ----------------------------------------------------------------------
FROM eclipse-temurin:17-jre

# JVM arguments can be overridden at runtime with `-e JAVA_OPTS="..."`
ENV JAVA_OPTS=""
WORKDIR /app

# Copy the shaded / fat JAR produced by the build stage
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# Use a small wrapper to allow additional JVM arguments
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 