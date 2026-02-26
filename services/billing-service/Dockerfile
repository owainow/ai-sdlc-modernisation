# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle/ gradle/
COPY libs/ libs/
COPY services/billing-service/ services/billing-service/
RUN chmod +x gradlew && ./gradlew :services:billing-service:bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
RUN groupadd -r appuser && useradd -r -g appuser -d /app appuser
WORKDIR /app
COPY --from=builder /app/services/billing-service/build/libs/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
