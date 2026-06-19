package com.campus.secondhand.admin.client;

import com.campus.secondhand.common.core.result.ApiResponse;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "campus-message-service")
public interface NotificationClient {

    @PostMapping("/api/notifications/system")
    ApiResponse<Void> create(@RequestBody Map<String, Object> request);
}
