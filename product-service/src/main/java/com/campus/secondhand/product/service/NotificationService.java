package com.campus.secondhand.product.service;

import com.campus.secondhand.product.vo.NotificationVO;
import java.util.List;

public interface NotificationService {

    List<NotificationVO> listMy();

    int unreadCount();

    void markRead(Long id);

    void create(Long userId, String title, String content);
}
