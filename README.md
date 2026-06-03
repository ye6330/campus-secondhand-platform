# campus-secondhand-platform

Microservice-based campus secondhand trading platform built with Java 11, Spring Boot, Spring Cloud Alibaba, Maven, and Vue 3.

## Modules

- `gateway-service`
- `user-service`
- `product-service`
- `order-service`
- `message-service`
- `admin-service`
- `common/common-core`
- `common/common-web`
- `common/common-security`

## Suggested next steps

1. Configure Nacos, MySQL, Redis, and RabbitMQ addresses in each service `bootstrap.yml`.
2. Import the root `pom.xml` into IntelliJ IDEA as a Maven project.
3. Start `gateway-service` and `user-service` first, then continue with business modules.
