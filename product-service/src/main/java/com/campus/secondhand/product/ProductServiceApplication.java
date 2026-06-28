package com.campus.secondhand.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@MapperScan({"com.campus.secondhand.product.mapper", "com.campus.secondhand.common.web.mapper"})
@SpringBootApplication(scanBasePackages = {"com.campus.secondhand.product", "com.campus.secondhand.common"})
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
