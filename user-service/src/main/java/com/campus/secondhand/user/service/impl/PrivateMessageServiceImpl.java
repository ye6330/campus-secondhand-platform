package com.campus.secondhand.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.user.dto.SendPrivateMessageRequest;
import com.campus.secondhand.user.entity.PrivateMessage;
import com.campus.secondhand.user.entity.User;
import com.campus.secondhand.user.mapper.PrivateMessageMapper;
import com.campus.secondhand.user.mapper.UserMapper;
import com.campus.secondhand.user.service.PrivateMessageService;
import com.campus.secondhand.user.vo.ConversationVO;
import com.campus.secondhand.user.vo.PrivateMessageVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final PrivateMessageMapper privateMessageMapper;
    private final UserMapper userMapper;

    public PrivateMessageServiceImpl(PrivateMessageMapper privateMessageMapper, UserMapper userMapper) {
        this.privateMessageMapper = privateMessageMapper;
        this.userMapper = userMapper;
    }

    @Override
    public PrivateMessageVO send(SendPrivateMessageRequest request) {
        User currentUser = currentUser();
        if (currentUser.getId().equals(request.getToUserId())) {
            throw new RuntimeException("不能给自己发私信");
        }
        User targetUser = userMapper.selectById(request.getToUserId());
        if (targetUser == null) {
            throw new RuntimeException("接收方不存在");
        }
        PrivateMessage message = new PrivateMessage();
        message.setFromUserId(currentUser.getId());
        message.setFromUsername(currentUser.getUsername());
        message.setFromNickname(currentUser.getNickname());
        message.setToUserId(targetUser.getId());
        message.setToUsername(targetUser.getUsername());
        message.setToNickname(targetUser.getNickname());
        message.setContent(request.getContent().trim());
        message.setReadStatus(0);
        message.setCreatedAt(LocalDateTime.now());
        privateMessageMapper.insert(message);
        return toVO(message, currentUser.getId());
    }

    @Override
    public List<ConversationVO> conversations() {
        Long currentUserId = currentUserId();
        List<PrivateMessage> messages = privateMessageMapper.selectList(
            new LambdaQueryWrapper<PrivateMessage>()
                .and(wrapper -> wrapper.eq(PrivateMessage::getFromUserId, currentUserId)
                    .or().eq(PrivateMessage::getToUserId, currentUserId))
                .orderByDesc(PrivateMessage::getCreatedAt)
        );
        Map<Long, ConversationVO> conversationMap = new LinkedHashMap<>();
        for (PrivateMessage message : messages) {
            Long targetUserId = message.getFromUserId().equals(currentUserId) ? message.getToUserId() : message.getFromUserId();
            if (!conversationMap.containsKey(targetUserId)) {
                ConversationVO conversation = new ConversationVO();
                User targetUser = userMapper.selectById(targetUserId);
                conversation.setTargetUserId(targetUserId);
                conversation.setTargetUsername(targetUser == null ? "" : targetUser.getUsername());
                conversation.setTargetNickname(targetUser == null ? "用户已不存在" : targetUser.getNickname());
                conversation.setTargetAvatar(targetUser == null ? "" : targetUser.getAvatar());
                conversation.setTargetPhone(targetUser == null ? "" : targetUser.getPhone());
                conversation.setLastMessage(message.getContent());
                conversation.setLastMessageTime(formatTime(message.getCreatedAt()));
                conversation.setUnreadCount(0);
                conversationMap.put(targetUserId, conversation);
            }
            if (message.getToUserId().equals(currentUserId) && Integer.valueOf(0).equals(message.getReadStatus())) {
                ConversationVO conversation = conversationMap.get(targetUserId);
                conversation.setUnreadCount(conversation.getUnreadCount() + 1);
            }
        }
        return new ArrayList<>(conversationMap.values());
    }

    @Override
    public List<PrivateMessageVO> detail(Long targetUserId) {
        Long currentUserId = currentUserId();
        List<PrivateMessage> messages = privateMessageMapper.selectList(
            new LambdaQueryWrapper<PrivateMessage>()
                .and(wrapper -> wrapper
                    .and(left -> left.eq(PrivateMessage::getFromUserId, currentUserId).eq(PrivateMessage::getToUserId, targetUserId))
                    .or(right -> right.eq(PrivateMessage::getFromUserId, targetUserId).eq(PrivateMessage::getToUserId, currentUserId)))
                .orderByAsc(PrivateMessage::getCreatedAt)
        );
        privateMessageMapper.update(null, new LambdaUpdateWrapper<PrivateMessage>()
            .eq(PrivateMessage::getFromUserId, targetUserId)
            .eq(PrivateMessage::getToUserId, currentUserId)
            .eq(PrivateMessage::getReadStatus, 0)
            .set(PrivateMessage::getReadStatus, 1));
        List<PrivateMessageVO> result = new ArrayList<>();
        for (PrivateMessage message : messages) {
            if (message.getToUserId().equals(currentUserId)) {
                message.setReadStatus(1);
            }
            result.add(toVO(message, currentUserId));
        }
        return result;
    }

    @Override
    public Map<String, Integer> unreadCount() {
        Long currentUserId = currentUserId();
        Integer count = Math.toIntExact(privateMessageMapper.selectCount(
            new LambdaQueryWrapper<PrivateMessage>()
                .eq(PrivateMessage::getToUserId, currentUserId)
                .eq(PrivateMessage::getReadStatus, 0)
        ));
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return result;
    }

    private PrivateMessageVO toVO(PrivateMessage message, Long currentUserId) {
        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setId(message.getId());
        vo.setFromUserId(message.getFromUserId());
        vo.setFromUsername(message.getFromUsername());
        vo.setFromNickname(message.getFromNickname());
        vo.setToUserId(message.getToUserId());
        vo.setToUsername(message.getToUsername());
        vo.setToNickname(message.getToNickname());
        vo.setContent(message.getContent());
        vo.setMine(message.getFromUserId().equals(currentUserId));
        vo.setRead(Integer.valueOf(1).equals(message.getReadStatus()));
        vo.setCreatedAt(formatTime(message.getCreatedAt()));
        return vo;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(TIME_FORMATTER);
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        return userId;
    }

    private User currentUser() {
        User user = userMapper.selectById(currentUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}
