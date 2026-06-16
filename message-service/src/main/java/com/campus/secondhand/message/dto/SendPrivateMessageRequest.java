package com.campus.secondhand.message.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class SendPrivateMessageRequest {

    @NotNull
    private Long toUserId;

    @NotBlank
    private String content;

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
