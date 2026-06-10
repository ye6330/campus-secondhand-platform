package com.campus.secondhand.order.vo;

import java.math.BigDecimal;

public class OrderVO {

    private Long id;
    private String orderNo;
    private Long productId;
    private String productTitle;
    private String productCoverImage;
    private BigDecimal productPrice;
    private Long buyerId;
    private String buyerName;
    private String buyerPhone;
    private Long sellerId;
    private String sellerName;
    private String sellerPhone;
    private String status;
    private String note;
    private String tradeImage;
    private String tradeConfirmedAt;
    private String createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
    public String getProductCoverImage() { return productCoverImage; }
    public void setProductCoverImage(String productCoverImage) { this.productCoverImage = productCoverImage; }
    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public String getSellerPhone() { return sellerPhone; }
    public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getTradeImage() { return tradeImage; }
    public void setTradeImage(String tradeImage) { this.tradeImage = tradeImage; }
    public String getTradeConfirmedAt() { return tradeConfirmedAt; }
    public void setTradeConfirmedAt(String tradeConfirmedAt) { this.tradeConfirmedAt = tradeConfirmedAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
