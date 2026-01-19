# Multi-stage build để giảm kích thước image
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml và tải dependencies trước (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR từ build stage
COPY --from=build /app/target/cinema-backend-*.jar app.jar

# Expose port
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=production"]

