package com.campus.secondhand.user.vo;

public class ConversationVO {

    private Long targetUserId;
    private String targetUsername;
    private String targetNickname;
    private String targetAvatar;
    private String targetPhone;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }
    public String getTargetNickname() { return targetNickname; }
    public void setTargetNickname(String targetNickname) { this.targetNickname = targetNickname; }
    public String getTargetAvatar() { return targetAvatar; }
    public void setTargetAvatar(String targetAvatar) { this.targetAvatar = targetAvatar; }
    public String getTargetPhone() { return targetPhone; }
    public void setTargetPhone(String targetPhone) { this.targetPhone = targetPhone; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
}
