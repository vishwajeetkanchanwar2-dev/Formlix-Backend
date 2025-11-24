# 1. Use Maven with JDK 21 for build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

# 2. Set working directory
WORKDIR /app

# 3. Copy project files
COPY . .

# 4. Build the Spring Boot app
RUN mvn clean install -DskipTests

# 5. Use a slim JDK 21 base image to run the app
FROM eclipse-temurin:21-jdk

# 6. Set working directory again
WORKDIR /app

# 7. Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Expose informational port
EXPOSE 10000

# 9. Start the Spring Boot application using Render's PORT
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
