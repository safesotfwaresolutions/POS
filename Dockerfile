# Multi-stage Dockerfile for Spring Boot Maven Application (Java 21)

# ==========================================================
# 1. Base Development & Dependency Cache Stage
# ==========================================================
FROM eclipse-temurin:21-jdk AS dev
WORKDIR /app

# Copy Maven wrapper configuration and dependency descriptor
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Clean line endings of mvnw script (crucial when building from Windows hosts)
RUN tr -d '\r' < mvnw > mvnw.lf && mv mvnw.lf mvnw && chmod +x mvnw

# Download dependencies offline to cache them in Docker layer
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Expose ports: 8080 for web application, 5005 for remote debugging
EXPOSE 8080 5005

# Command to run in development mode (enables Spring Boot DevTools hot reload and remote debug)
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]


# ==========================================================
# 2. Builder Stage for Production Packaging
# ==========================================================
FROM dev AS builder
# Package the application as a standalone executable JAR
RUN ./mvnw package -DskipTests


# ==========================================================
# 3. Production Stage
# ==========================================================
FROM eclipse-temurin:21-jre-jammy AS prod
WORKDIR /app

# Create a secure, non-privileged system user/group to run the application
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership to the non-root user
RUN chown -R spring:spring /app
USER spring:spring

# Expose production port
EXPOSE 8080

# Run the packaged JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
