package com.campus.secondhand.order.dto;

import javax.validation.constraints.Size;

public class HandleOrderRequest {

    @Size(max = 200)
    private String note;

    private String tradeImage;

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getTradeImage() { return tradeImage; }
    public void setTradeImage(String tradeImage) { this.tradeImage = tradeImage; }
}
