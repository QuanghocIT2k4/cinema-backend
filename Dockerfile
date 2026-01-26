# Multi-stage build để giảm kích thước image
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml và tải dependencies trước (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR từ build stage
COPY --from=build /app/target/cinema-backend-*.jar app.jar

# Expose port (Render tự động map PORT env var)
EXPOSE 8080

# Run app với production profile
# Render tự động set PORT env var
# SPRING_PROFILES_ACTIVE sẽ được set từ Render env vars (set trong Environment tab)
ENTRYPOINT ["java", "-jar", "app.jar"]








