package com.campus.secondhand.user.vo;

public class PrivateMessageVO {

    private Long id;
    private Long fromUserId;
    private String fromUsername;
    private String fromNickname;
    private Long toUserId;
    private String toUsername;
    private String toNickname;
    private String content;
    private Boolean mine;
    private Boolean read;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
    public String getFromUsername() { return fromUsername; }
    public void setFromUsername(String fromUsername) { this.fromUsername = fromUsername; }
    public String getFromNickname() { return fromNickname; }
    public void setFromNickname(String fromNickname) { this.fromNickname = fromNickname; }
    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
    public String getToUsername() { return toUsername; }
    public void setToUsername(String toUsername) { this.toUsername = toUsername; }
    public String getToNickname() { return toNickname; }
    public void setToNickname(String toNickname) { this.toNickname = toNickname; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getMine() { return mine; }
    public void setMine(Boolean mine) { this.mine = mine; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
