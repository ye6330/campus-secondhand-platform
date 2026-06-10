package com.campus.secondhand.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.campus.secondhand.order.mapper")
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.campus.secondhand.order", "com.campus.secondhand.common"})
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
