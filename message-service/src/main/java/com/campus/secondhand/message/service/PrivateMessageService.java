package com.campus.secondhand.message.service;

import com.campus.secondhand.message.dto.SendPrivateMessageRequest;
import com.campus.secondhand.message.vo.ConversationVO;
import com.campus.secondhand.message.vo.PrivateMessageVO;
import java.util.List;
import java.util.Map;

public interface PrivateMessageService {

    PrivateMessageVO send(SendPrivateMessageRequest request);

    List<ConversationVO> conversations();

    List<PrivateMessageVO> detail(Long targetUserId);

    Map<String, Integer> unreadCount();
}
