FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/message-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9104
ENTRYPOINT ["java", "-jar", "app.jar"]
