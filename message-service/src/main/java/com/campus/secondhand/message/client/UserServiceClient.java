package com.campus.secondhand.message.client;

import com.campus.secondhand.common.core.result.ApiResponse;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserServiceClient {

    private static final String USER_SERVICE_URL = "http://localhost:9101";

    private final RestTemplate restTemplate;

    public UserServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    public ApiResponse<Map<String, Object>> getUserContact(Long userId) {
        String authHeader = getAuthHeader();
        if (authHeader == null || authHeader.isEmpty()) {
            return ApiResponse.failed(401, "未登录");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", authHeader);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            return restTemplate.exchange(
                USER_SERVICE_URL + "/api/users/" + userId + "/contact",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {}
            ).getBody();
        } catch (Exception e) {
            return ApiResponse.failed(500, e.getMessage());
        }
    }

    private String getAuthHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return request.getHeader("Authorization");
    }
}
