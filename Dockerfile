# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Copy source code
COPY src ./src

# Build application
RUN gradle clean build -x test --no-daemon

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/build/libs/ecms-server-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 7777

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]