package com.campus.secondhand.product.dto;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UpdateProductRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 50, message = "商品标题不能超过50个字符")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    @Size(max = 500, message = "商品描述不能超过500个字符")
    private String description;

    @NotBlank(message = "商品封面不能为空")
    private String coverImage;

    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

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

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
