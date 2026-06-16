package com.campus.secondhand.order.client;

import com.campus.secondhand.order.dto.CreateNotificationRequest;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificationClient", url = "${ADMIN_SERVICE_URL:http://localhost:9105}")
public interface NotificationClient {

    @PostMapping("/api/notifications/system")
    Map<String, Object> create(@RequestBody CreateNotificationRequest request);
}
