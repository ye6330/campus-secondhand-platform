package com.campus.secondhand.order.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "campus-user-service")
public interface UserClient {

    @GetMapping("/api/users/{id}/contact")
    Map<String, Object> contact(@PathVariable("id") Long id);
}
