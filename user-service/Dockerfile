FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/user-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9101
ENTRYPOINT ["java", "-jar", "app.jar"]
