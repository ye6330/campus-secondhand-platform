FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/product-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 9102
ENTRYPOINT ["java", "-jar", "app.jar"]
