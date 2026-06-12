package com.campus.secondhand.user.service;

import com.campus.secondhand.user.dto.SendPrivateMessageRequest;
import com.campus.secondhand.user.vo.ConversationVO;
import com.campus.secondhand.user.vo.PrivateMessageVO;
import java.util.List;
import java.util.Map;

public interface PrivateMessageService {

    PrivateMessageVO send(SendPrivateMessageRequest request);

    List<ConversationVO> conversations();

    List<PrivateMessageVO> detail(Long targetUserId);

    Map<String, Integer> unreadCount();
}
