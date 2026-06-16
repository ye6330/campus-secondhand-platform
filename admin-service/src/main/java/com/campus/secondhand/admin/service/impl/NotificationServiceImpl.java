package com.campus.secondhand.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.admin.entity.Notification;
import com.campus.secondhand.admin.mapper.NotificationMapper;
import com.campus.secondhand.admin.service.NotificationService;
import com.campus.secondhand.admin.vo.NotificationVO;
import com.campus.secondhand.common.security.context.UserContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public List<NotificationVO> listMy() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        return notificationMapper.selectList(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt)
        ).stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public int unreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        Long count = notificationMapper.selectCount(
            new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getReadStatus, 0)
        );
        return count == null ? 0 : count.intValue();
    }

    @Override
    public void markRead(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        Notification notification = notificationMapper.selectById(id);
        if (notification == null || !userId.equals(notification.getUserId())) {
            throw new RuntimeException("通知不存在");
        }
        notification.setReadStatus(1);
        notificationMapper.updateById(notification);
    }

    @Override
    public void create(Long userId, String title, String content) {
        if (userId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setReadStatus(0);
        notification.setCreatedAt(LocalDateTime.now());
        notificationMapper.insert(notification);
    }

    private NotificationVO toVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setRead(notification.getReadStatus() != null && notification.getReadStatus() == 1);
        if (notification.getCreatedAt() != null) {
            vo.setCreatedAt(notification.getCreatedAt().format(TIME_FORMATTER));
        }
        return vo;
    }
}
