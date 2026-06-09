package com.campus.secondhand.product.service;

import com.campus.secondhand.product.dto.CreateCommentRequest;
import com.campus.secondhand.product.vo.CommentVO;
import java.util.List;

public interface CommentService {

    List<CommentVO> listByProductId(Long productId);

    CommentVO create(CreateCommentRequest request);

    void deleteById(Long id);
}
