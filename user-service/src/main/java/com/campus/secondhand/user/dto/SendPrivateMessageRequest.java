package com.campus.secondhand.user.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SendPrivateMessageRequest {

    @NotNull
    private Long toUserId;

    @NotBlank
    @Size(max = 500)
    private String content;

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
