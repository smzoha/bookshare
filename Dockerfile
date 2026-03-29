# Phase 1 :: Build
FROM gradle:jdk25 AS builder

WORKDIR /app

# Copy Gradle wrapper
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew

# Copy dependencies
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon

# Copy src
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Phase 2 :: Run
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create logs folder and setup ownership
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

# Copy JAR from build phase
COPY --from=builder /app/build/libs/*.jar bookshare.jar
RUN chown appuser:appgroup bookshare.jar

USER appuser

EXPOSE 6001

ENTRYPOINT ["java", "-ea", "-jar", "bookshare.jar"]
