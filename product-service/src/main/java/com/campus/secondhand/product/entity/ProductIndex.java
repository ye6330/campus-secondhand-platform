package com.campus.secondhand.product.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Document(indexName = "product")
public class ProductIndex {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Keyword)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private String coverImage;

    @Field(type = FieldType.Keyword)
    private Long sellerId;

    @Field(type = FieldType.Keyword)
    private String sellerName;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public static ProductIndex from(Product product) {
        ProductIndex index = new ProductIndex();
        index.setId(product.getId());
        index.setTitle(product.getTitle());
        index.setDescription(product.getDescription());
        index.setPrice(product.getPrice());
        index.setCoverImage(product.getCoverImage());
        index.setSellerId(product.getSellerId());
        index.setSellerName(product.getSellerName());
        index.setStatus(product.getStatus());
        index.setCreatedAt(product.getCreatedAt() == null ? null : product.getCreatedAt().format(TIME_FORMATTER));
        return index;
    }
}