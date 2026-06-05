package com.campus.secondhand.product.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.product.service.FavoriteService;
import com.campus.secondhand.product.vo.ProductVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "商品收藏")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @ApiOperation("收藏商品")
    @PostMapping("/{productId}")
    public ApiResponse<Void> add(@PathVariable Long productId) {
        favoriteService.add(productId);
        return ApiResponse.success(null);
    }

    @ApiOperation("取消收藏")
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> remove(@PathVariable Long productId) {
        favoriteService.remove(productId);
        return ApiResponse.success(null);
    }

    @ApiOperation("我的收藏列表")
    @GetMapping("/my")
    public ApiResponse<List<ProductVO>> listMy() {
        return ApiResponse.success(favoriteService.listMy());
    }

    @ApiOperation("批量取消收藏")
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchRemove(@RequestBody List<Long> productIds) {
        favoriteService.batchRemove(productIds);
        return ApiResponse.success(null);
    }
}
