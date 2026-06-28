package com.campus.secondhand.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.admin.service.OperationLogService;
import com.campus.secondhand.admin.vo.OperationLogVO;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.common.web.entity.OperationLogRecord;
import com.campus.secondhand.common.web.mapper.OperationLogRecordMapper;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final OperationLogRecordMapper operationLogRecordMapper;

    public OperationLogServiceImpl(OperationLogRecordMapper operationLogRecordMapper) {
        this.operationLogRecordMapper = operationLogRecordMapper;
    }

    @Override
    public List<OperationLogVO> list(String action, String result) {
        if (!"ADMIN".equals(UserContext.getRole())) {
            throw new RuntimeException("无权查看操作日志");
        }
        LambdaQueryWrapper<OperationLogRecord> wrapper = new LambdaQueryWrapper<OperationLogRecord>()
            .orderByDesc(OperationLogRecord::getCreatedAt);
        if (action != null && !action.trim().isEmpty()) {
            wrapper.like(OperationLogRecord::getAction, action.trim());
        }
        if (result != null && !result.trim().isEmpty()) {
            wrapper.eq(OperationLogRecord::getResult, result.trim());
        }
        return operationLogRecordMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    private OperationLogVO toVO(OperationLogRecord record) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(record.getId());
        vo.setAction(record.getAction());
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setOperatorRole(record.getOperatorRole());
        vo.setMethodName(record.getMethodName());
        vo.setParams(record.getParams());
        vo.setResult(record.getResult());
        vo.setErrorMessage(record.getErrorMessage());
        vo.setDurationMs(record.getDurationMs());
        if (record.getCreatedAt() != null) {
            vo.setCreatedAt(record.getCreatedAt().format(TIME_FORMATTER));
        }
        return vo;
    }
}
