package com.campus.secondhand.admin.controller;

import com.campus.secondhand.admin.dto.CreateReportRequest;
import com.campus.secondhand.admin.dto.HandleReportRequest;
import com.campus.secondhand.admin.service.ReportService;
import com.campus.secondhand.admin.vo.ReportVO;
import com.campus.secondhand.common.core.result.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "商品举报")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @ApiOperation("提交举报")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateReportRequest request) {
        reportService.create(request);
        return ApiResponse.success(null);
    }

    @ApiOperation("管理员查看举报列表")
    @GetMapping("/admin")
    public ApiResponse<List<ReportVO>> listAdmin(@RequestParam(required = false) String status) {
        return ApiResponse.success(reportService.listAdmin(status));
    }

    @ApiOperation("管理员处理举报")
    @PutMapping("/{id}/handle")
    public ApiResponse<Void> handle(@PathVariable Long id, @RequestParam String action,
        @RequestBody(required = false) HandleReportRequest request) {
        reportService.handle(id, action, request);
        return ApiResponse.success(null);
    }
}
