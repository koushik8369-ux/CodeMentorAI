# ==============================================================================
# CodeMentor AI - Production Spring Boot Backend Dockerfile (Java 21)
# Stateless & Database-Free Architecture
# ==============================================================================

# ------------------------------------------------------------------------------
# Build Stage
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-jammy AS builder

WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Runtime Stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runner

# Install curl for health checking
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Run as non-root user for container security
RUN groupadd -r codementor && useradd -r -g codementor -d /app -s /sbin/nologin codementor

WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R codementor:codementor /app

USER codementor

# Default port (overridden by Cloud Run, Render, Railway, Heroku via $PORT)
ENV PORT=8080
EXPOSE 8080

# Production JVM flags optimized for container memory constraints
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check against the zero-DB health endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT}/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
