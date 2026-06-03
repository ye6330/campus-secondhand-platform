package com.campus.secondhand.order.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @ApiOperation("查询订单列表")
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.success(List.of(
                Map.of("orderNo", "ORD2026001", "status", "PENDING"),
                Map.of("orderNo", "ORD2026002", "status", "COMPLETED")
        ));
    }
}
