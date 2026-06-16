package com.campus.secondhand.message.client;

import com.campus.secondhand.common.core.result.ApiResponse;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "campus-user-service", url = "${user-service.url:http://localhost:9101}")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}/contact")
    ApiResponse<Map<String, Object>> getUserContact(@PathVariable("id") Long id);
}
