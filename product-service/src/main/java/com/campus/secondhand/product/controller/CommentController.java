package com.campus.secondhand.product.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.product.dto.CreateCommentRequest;
import com.campus.secondhand.product.service.CommentService;
import com.campus.secondhand.product.vo.CommentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "商品评论")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @ApiOperation("商品评论列表")
    @GetMapping
    public ApiResponse<List<CommentVO>> list(@RequestParam Long productId) {
        return ApiResponse.success(commentService.listByProductId(productId));
    }

    @ApiOperation("发表评论")
    @PostMapping
    public ApiResponse<CommentVO> create(@Valid @RequestBody CreateCommentRequest request) {
        return ApiResponse.success(commentService.create(request));
    }

    @ApiOperation("删除自己的留言")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        commentService.deleteById(id);
        return ApiResponse.success(null);
    }
}
