package com.campus.secondhand.admin.service;

import com.campus.secondhand.admin.dto.CreateReportRequest;
import com.campus.secondhand.admin.dto.HandleReportRequest;
import com.campus.secondhand.admin.vo.ReportVO;
import java.util.List;

public interface ReportService {

    void create(CreateReportRequest request);

    List<ReportVO> listAdmin(String status);

    void handle(Long id, String action, HandleReportRequest request);
}
