package com.campus.secondhand.order.client;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "campus-product-service")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    Map<String, Object> detail(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/internal/trading")
    Map<String, Object> markTrading(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/internal/on-shelf")
    Map<String, Object> restoreOnShelf(@PathVariable("id") Long id);

    @PutMapping("/api/products/{id}/internal/sold")
    Map<String, Object> markSold(@PathVariable("id") Long id);
}
