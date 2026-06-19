package com.campus.secondhand.message.service;

import com.campus.secondhand.message.vo.NotificationVO;
import java.util.List;

public interface NotificationService {

    List<NotificationVO> listMy();

    int unreadCount();

    void markRead(Long id);

    void create(Long userId, String title, String content);
}
