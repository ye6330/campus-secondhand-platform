FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/gateway-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
