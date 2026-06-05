package com.campus.secondhand.product.service;

import com.campus.secondhand.product.dto.CreateProductRequest;
import com.campus.secondhand.product.dto.UpdateProductRequest;
import com.campus.secondhand.product.vo.ProductVO;
import java.util.List;

public interface ProductService {

    List<ProductVO> list();

    List<ProductVO> listMy(Long userId);

    ProductVO getById(Long id);

    ProductVO create(CreateProductRequest request);

    ProductVO update(Long id, UpdateProductRequest request);

    void deleteById(Long id);

    List<ProductVO> listPending();

    void review(Long id, String action);
}
