package com.campus.secondhand.product.service;

import com.campus.secondhand.product.dto.CreateProductRequest;
import com.campus.secondhand.product.dto.UpdateProductRequest;
import com.campus.secondhand.product.vo.ProductVO;
import java.util.List;

public interface ProductService {

    List<ProductVO> list(String keyword);

    List<String> suggest(String keyword);

    List<ProductVO> listMy(Long userId, String status);

    ProductVO getById(Long id);

    ProductVO create(CreateProductRequest request);

    ProductVO update(Long id, UpdateProductRequest request);

    void deleteById(Long id);

    List<ProductVO> listPending();

    void review(Long id, String action);
}
