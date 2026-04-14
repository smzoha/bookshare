# syntax=docker/dockerfile:1

# Phase 1 :: Build
FROM gradle:jdk25 AS builder

WORKDIR /app

# Copy Gradle wrapper
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew

# Copy dependency specs first - this layer is only invalidated when build files change
COPY build.gradle settings.gradle ./

# Copy src
COPY src ./src

# Build JAR - Gradle cache is mounted so dependencies are not re-downloaded between builds
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon --build-cache -x test

# Phase 2 :: Run
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user, logs directory, and set ownership in a single layer
RUN addgroup -S appgroup \
    && adduser -S appuser -G appgroup \
    && mkdir -p /app/logs \
    && chown -R appuser:appgroup /app

# Copy JAR from build phase with ownership set at copy time (avoids an extra layer)
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar bookshare.jar

USER appuser

EXPOSE 6001

# UseContainerSupport: respect cgroup memory limits (default on Java 11+, explicit is clearer)
# MaxRAMPercentage: cap heap at 75% of container memory, leaving headroom for off-heap/metaspace
ENTRYPOINT ["java", "-ea", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "bookshare.jar"]
