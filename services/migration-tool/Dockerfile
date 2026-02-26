# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/
COPY libs/ libs/
COPY services/migration-tool/ services/migration-tool/
RUN chmod +x gradlew && ./gradlew :services:migration-tool:bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser -d /app appuser
WORKDIR /app
COPY --from=builder /app/services/migration-tool/build/libs/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
