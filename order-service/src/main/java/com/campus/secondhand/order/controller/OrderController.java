package com.campus.secondhand.order.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.order.dto.CreateOrderRequest;
import com.campus.secondhand.order.dto.HandleOrderRequest;
import com.campus.secondhand.order.service.OrderService;
import com.campus.secondhand.order.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApiOperation("发起购买意向")
    @PostMapping
    public ApiResponse<OrderVO> create(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(orderService.create(request));
    }

    @ApiOperation("我的买入订单")
    @GetMapping("/buy")
    public ApiResponse<List<OrderVO>> listMyBuy(@RequestParam(required = false) String status) {
        return ApiResponse.success(orderService.listMyBuy(status));
    }

    @ApiOperation("我的卖出订单")
    @GetMapping("/sell")
    public ApiResponse<List<OrderVO>> listMySell(@RequestParam(required = false) String status) {
        return ApiResponse.success(orderService.listMySell(status));
    }

    @ApiOperation("管理员查看订单列表")
    @GetMapping("/admin")
    public ApiResponse<List<OrderVO>> listAdmin(@RequestParam(required = false) String status) {
        return ApiResponse.success(orderService.listAdmin(status));
    }

    @ApiOperation("卖家确认订单")
    @PutMapping("/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long id, @RequestBody(required = false) HandleOrderRequest request) {
        orderService.confirm(id, request);
        return ApiResponse.success(null);
    }

    @ApiOperation("卖家拒绝订单")
    @PutMapping("/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody(required = false) HandleOrderRequest request) {
        orderService.reject(id, request);
        return ApiResponse.success(null);
    }

    @ApiOperation("买家取消订单")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return ApiResponse.success(null);
    }
}
