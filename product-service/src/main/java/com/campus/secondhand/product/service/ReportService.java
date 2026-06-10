package com.campus.secondhand.product.service;

import com.campus.secondhand.product.dto.CreateReportRequest;
import com.campus.secondhand.product.dto.HandleReportRequest;
import com.campus.secondhand.product.vo.ReportVO;
import java.util.List;

public interface ReportService {

    void create(CreateReportRequest request);

    List<ReportVO> listAdmin(String status);

    void handle(Long id, String action, HandleReportRequest request);
}
