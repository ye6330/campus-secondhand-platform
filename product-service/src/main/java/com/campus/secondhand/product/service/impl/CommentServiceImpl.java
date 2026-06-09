package com.campus.secondhand.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.product.dto.CreateCommentRequest;
import com.campus.secondhand.product.entity.Comment;
import com.campus.secondhand.product.entity.Product;
import com.campus.secondhand.product.mapper.CommentMapper;
import com.campus.secondhand.product.mapper.ProductMapper;
import com.campus.secondhand.product.service.CommentService;
import com.campus.secondhand.product.vo.CommentVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final CommentMapper commentMapper;
    private final ProductMapper productMapper;

    public CommentServiceImpl(CommentMapper commentMapper, ProductMapper productMapper) {
        this.commentMapper = commentMapper;
        this.productMapper = productMapper;
    }

    @Override
    public List<CommentVO> listByProductId(Long productId) {
        Long currentUserId = UserContext.getUserId();
        return commentMapper.selectList(
            new LambdaQueryWrapper<Comment>()
                .eq(Comment::getProductId, productId)
                .orderByDesc(Comment::getCreatedAt)
        ).stream().map(comment -> toVO(comment, currentUserId)).collect(Collectors.toList());
    }

    @Override
    public CommentVO create(CreateCommentRequest request) {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        if (userId == null || username == null) {
            throw new RuntimeException("未登录");
        }
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setProductId(request.getProductId());
        comment.setContent(request.getContent().trim());
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        return toVO(comment, userId);
    }

    @Override
    public void deleteById(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("留言不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new RuntimeException("只能删除自己的留言");
        }
        commentMapper.deleteById(id);
    }

    private CommentVO toVO(Comment comment, Long currentUserId) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setUserId(comment.getUserId());
        vo.setUsername(comment.getUsername());
        vo.setProductId(comment.getProductId());
        vo.setContent(comment.getContent());
        vo.setMine(currentUserId != null && currentUserId.equals(comment.getUserId()));
        if (comment.getCreatedAt() != null) {
            vo.setCreatedAt(comment.getCreatedAt().format(TIME_FORMATTER));
        }
        return vo;
    }
}
