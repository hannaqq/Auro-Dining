# Auro Dining - Spring Boot application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR (path matches deploy layout)
COPY auro-dining-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 80

ENTRYPOINT ["java", \
  "-Xms256m", "-Xmx512m", \
  "-XX:+UseG1GC", \
  "-jar", "app.jar"]
