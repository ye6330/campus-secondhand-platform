package com.campus.secondhand.message.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.message.dto.SendPrivateMessageRequest;
import com.campus.secondhand.message.service.PrivateMessageService;
import com.campus.secondhand.message.vo.ConversationVO;
import com.campus.secondhand.message.vo.PrivateMessageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "私信管理")
@RestController
@RequestMapping("/api/messages")
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;

    public PrivateMessageController(PrivateMessageService privateMessageService) {
        this.privateMessageService = privateMessageService;
    }

    @ApiOperation("发送私信")
    @PostMapping
    public ApiResponse<PrivateMessageVO> send(@Valid @RequestBody SendPrivateMessageRequest request) {
        return ApiResponse.success(privateMessageService.send(request));
    }

    @ApiOperation("会话列表")
    @GetMapping("/conversations")
    public ApiResponse<List<ConversationVO>> conversations() {
        return ApiResponse.success(privateMessageService.conversations());
    }

    @ApiOperation("会话详情")
    @GetMapping("/conversations/{targetUserId}")
    public ApiResponse<List<PrivateMessageVO>> detail(@PathVariable Long targetUserId) {
        return ApiResponse.success(privateMessageService.detail(targetUserId));
    }

    @ApiOperation("未读私信数量")
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Integer>> unreadCount() {
        return ApiResponse.success(privateMessageService.unreadCount());
    }
}
