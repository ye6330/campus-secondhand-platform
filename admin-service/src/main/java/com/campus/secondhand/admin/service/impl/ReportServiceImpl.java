package com.campus.secondhand.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.admin.dto.CreateReportRequest;
import com.campus.secondhand.admin.dto.HandleReportRequest;
import com.campus.secondhand.admin.entity.Product;
import com.campus.secondhand.admin.entity.Report;
import com.campus.secondhand.admin.mapper.ProductMapper;
import com.campus.secondhand.admin.mapper.ReportMapper;
import com.campus.secondhand.admin.service.NotificationService;
import com.campus.secondhand.admin.service.ReportService;
import com.campus.secondhand.admin.vo.ReportVO;
import com.campus.secondhand.common.security.context.UserContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ReportMapper reportMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;

    public ReportServiceImpl(ReportMapper reportMapper, ProductMapper productMapper,
        NotificationService notificationService) {
        this.reportMapper = reportMapper;
        this.productMapper = productMapper;
        this.notificationService = notificationService;
    }

    @Override
    public void create(CreateReportRequest request) {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        if (userId == null || username == null) {
            throw new RuntimeException("未登录");
        }
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (userId.equals(product.getSellerId())) {
            throw new RuntimeException("不能举报自己的商品");
        }
        Long existing = reportMapper.selectCount(
            new LambdaQueryWrapper<Report>()
                .eq(Report::getProductId, request.getProductId())
                .eq(Report::getReporterId, userId)
                .eq(Report::getStatus, "待处理")
        );
        if (existing != null && existing > 0) {
            throw new RuntimeException("你已举报过该商品，请等待处理");
        }
        Report report = new Report();
        report.setProductId(product.getId());
        report.setProductTitle(product.getTitle());
        report.setReporterId(userId);
        report.setReporterName(username);
        report.setReason(request.getReason().trim());
        report.setStatus("待处理");
        report.setCreatedAt(LocalDateTime.now());
        reportMapper.insert(report);
    }

    @Override
    public List<ReportVO> listAdmin(String status) {
        String role = UserContext.getRole();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权查看举报记录");
        }
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<Report>()
            .orderByAsc(Report::getStatus)
            .orderByDesc(Report::getCreatedAt);
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Report::getStatus, status.trim());
        }
        return reportMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void handle(Long id, String action, HandleReportRequest request) {
        String role = UserContext.getRole();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权处理举报");
        }
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }
        if (!"待处理".equals(report.getStatus())) {
            throw new RuntimeException("该举报已处理");
        }
        Product product = productMapper.selectById(report.getProductId());
        if ("off_shelf".equals(action)) {
            if (product == null) {
                throw new RuntimeException("商品不存在，无法下架");
            }
            product.setStatus("已下架");
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.updateById(product);
            report.setStatus("违规已下架");
        } else if ("delete_product".equals(action)) {
            if (product == null) {
                throw new RuntimeException("商品不存在，无法删除");
            }
            productMapper.deleteById(product.getId());
            report.setStatus("违规已删除");
        } else if ("resolve".equals(action)) {
            report.setStatus("已处理");
        } else if ("reject".equals(action)) {
            report.setStatus("已驳回");
        } else {
            throw new RuntimeException("处理操作不正确");
        }
        report.setHandleNote(request == null || request.getHandleNote() == null ? null : request.getHandleNote().trim());
        report.setHandledAt(LocalDateTime.now());
        reportMapper.updateById(report);
        notifyUsers(report, product);
    }

    private void notifyUsers(Report report, Product product) {
        String resultText = report.getStatus();
        String noteText = report.getHandleNote() == null || report.getHandleNote().isEmpty()
            ? ""
            : "，处理说明：" + report.getHandleNote();
        notificationService.create(
            report.getReporterId(),
            "举报处理结果",
            "你对商品《" + report.getProductTitle() + "》的举报结果为：" + resultText + noteText
        );
        if (product != null && product.getSellerId() != null && !product.getSellerId().equals(report.getReporterId())
            && ("违规已下架".equals(report.getStatus()) || "违规已删除".equals(report.getStatus()))) {
            notificationService.create(
                product.getSellerId(),
                "商品违规处理通知",
                "你的商品《" + report.getProductTitle() + "》已被管理员处理，结果为：" + report.getStatus() + noteText
            );
        }
    }

    private ReportVO toVO(Report report) {
        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setProductId(report.getProductId());
        vo.setProductTitle(report.getProductTitle());
        vo.setReporterId(report.getReporterId());
        vo.setReporterName(report.getReporterName());
        vo.setReason(report.getReason());
        vo.setStatus(report.getStatus());
        vo.setHandleNote(report.getHandleNote());
        if (report.getCreatedAt() != null) {
            vo.setCreatedAt(report.getCreatedAt().format(TIME_FORMATTER));
        }
        if (report.getHandledAt() != null) {
            vo.setHandledAt(report.getHandledAt().format(TIME_FORMATTER));
        }
        return vo;
    }
}
