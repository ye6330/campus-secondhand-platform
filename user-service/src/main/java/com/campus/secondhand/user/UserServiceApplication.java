package com.campus.secondhand.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.campus.secondhand.user.mapper")
@SpringBootApplication(scanBasePackages = {"com.campus.secondhand.user", "com.campus.secondhand.common"})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
