package com.campus.secondhand.admin.service;

import com.campus.secondhand.admin.vo.NotificationVO;
import java.util.List;

public interface NotificationService {

    List<NotificationVO> listMy();

    int unreadCount();

    void markRead(Long id);

    void create(Long userId, String title, String content);
}
