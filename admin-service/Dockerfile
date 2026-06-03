FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/admin-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9105
ENTRYPOINT ["java", "-jar", "app.jar"]
