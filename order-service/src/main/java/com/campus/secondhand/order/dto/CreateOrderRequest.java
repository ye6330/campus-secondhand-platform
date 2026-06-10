package com.campus.secondhand.order.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateOrderRequest {

    @NotNull
    private Long productId;

    @Size(max = 200)
    private String note;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
