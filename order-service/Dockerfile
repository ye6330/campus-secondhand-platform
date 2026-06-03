FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/order-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9103
ENTRYPOINT ["java", "-jar", "app.jar"]
