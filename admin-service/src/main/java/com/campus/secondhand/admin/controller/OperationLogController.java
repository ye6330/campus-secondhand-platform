package com.campus.secondhand.admin.controller;

import com.campus.secondhand.admin.service.OperationLogService;
import com.campus.secondhand.admin.vo.OperationLogVO;
import com.campus.secondhand.common.core.result.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "操作日志")
@RestController
@RequestMapping("/api/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @ApiOperation("管理员查看操作日志")
    @GetMapping("/admin")
    public ApiResponse<List<OperationLogVO>> listAdmin(@RequestParam(required = false) String action,
        @RequestParam(required = false) String result) {
        return ApiResponse.success(operationLogService.list(action, result));
    }
}
