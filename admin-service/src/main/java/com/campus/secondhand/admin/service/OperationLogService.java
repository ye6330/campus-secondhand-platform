package com.campus.secondhand.admin.service;

import com.campus.secondhand.admin.vo.OperationLogVO;
import java.util.List;

public interface OperationLogService {

    List<OperationLogVO> list(String action, String result);
}
