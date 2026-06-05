package com.campus.secondhand.product.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// 发布商品时前端提交的数据
public class CreateProductRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 50, message = "商品标题不能超过50个字符")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    @Size(max = 500, message = "商品描述不能超过500个字符")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @NotBlank(message = "商品封面不能为空")
    private String coverImage;

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
}
