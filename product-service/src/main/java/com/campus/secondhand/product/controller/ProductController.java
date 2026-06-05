package com.campus.secondhand.product.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.product.dto.CreateProductRequest;
import com.campus.secondhand.product.dto.UpdateProductRequest;
import com.campus.secondhand.product.service.FileService;
import com.campus.secondhand.product.service.ProductService;
import com.campus.secondhand.product.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final FileService fileService;

    public ProductController(ProductService productService, FileService fileService) {
        this.productService = productService;
        this.fileService = fileService;
    }

    @ApiOperation("查询商品列表（只展示已上架）")
    @GetMapping
    public ApiResponse<List<ProductVO>> list() {
        return ApiResponse.success(productService.list());
    }

    @ApiOperation("查询我的商品（所有状态）")
    @GetMapping("/my")
    public ApiResponse<List<ProductVO>> listMy() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.failed(401, "未登录");
        }
        return ApiResponse.success(productService.listMy(userId));
    }

    @ApiOperation("商品详情")
    @GetMapping("/{id}")
    public ApiResponse<ProductVO> detail(@PathVariable Long id) {
        return ApiResponse.success(productService.getById(id));
    }

    @ApiOperation("发布商品（状态=待审核）")
    @PostMapping
    public ApiResponse<ProductVO> create(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.success(productService.create(request));
    }

    @ApiOperation("编辑商品（仅卖家，编辑后回到待审核）")
    @PutMapping("/{id}")
    public ApiResponse<ProductVO> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.success(productService.update(id, request));
    }

    @ApiOperation("删除商品（仅卖家）")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return ApiResponse.success(null);
    }

    @ApiOperation("待审核商品列表（仅admin）")
    @GetMapping("/pending")
    public ApiResponse<List<ProductVO>> listPending() {
        String role = UserContext.getRole();
        if (!"ADMIN".equals(role)) {
            return ApiResponse.failed(403, "无权访问");
        }
        return ApiResponse.success(productService.listPending());
    }

    @ApiOperation("审核商品（仅admin）")
    @PutMapping("/{id}/review")
    public ApiResponse<Void> review(@PathVariable Long id, @RequestParam String action) {
        productService.review(id, action);
        return ApiResponse.success(null);
    }

    @ApiOperation("上传商品封面图")
    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(fileService.uploadProductImage(file));
    }
}
