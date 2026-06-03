package com.campus.secondhand.product.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @ApiOperation("查询商品列表")
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.success(List.of(
                Map.of("id", 1, "title", "Secondhand Keyboard", "price", 129.0),
                Map.of("id", 2, "title", "Exam Books", "price", 35.0)
        ));
    }
}
