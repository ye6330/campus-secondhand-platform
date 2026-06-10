package com.campus.secondhand.product.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.product.dto.CreateNotificationRequest;
import com.campus.secondhand.product.service.NotificationService;
import com.campus.secondhand.product.vo.NotificationVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "站内通知")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ApiOperation("我的通知列表")
    @GetMapping("/my")
    public ApiResponse<List<NotificationVO>> listMy() {
        return ApiResponse.success(notificationService.listMy());
    }

    @ApiOperation("未读通知数量")
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Integer>> unreadCount() {
        return ApiResponse.success(java.util.Collections.singletonMap("count", notificationService.unreadCount()));
    }

    @ApiOperation("标记通知已读")
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ApiResponse.success(null);
    }

    @ApiOperation("创建系统通知")
    @PostMapping("/system")
    public ApiResponse<Void> createSystem(@Valid @RequestBody CreateNotificationRequest request) {
        notificationService.create(request.getUserId(), request.getTitle().trim(), request.getContent().trim());
        return ApiResponse.success(null);
    }
}
