package com.campus.secondhand.order.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "productClient", url = "${PRODUCT_SERVICE_URL:http://localhost:9102}")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    Map<String, Object> detail(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/sold")
    Map<String, Object> markSold(@PathVariable("id") Long id);
}
