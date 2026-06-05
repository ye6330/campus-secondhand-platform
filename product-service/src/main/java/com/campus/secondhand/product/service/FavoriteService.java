package com.campus.secondhand.product.service;

import com.campus.secondhand.product.vo.ProductVO;
import java.util.List;

public interface FavoriteService {

    void add(Long productId);

    void remove(Long productId);

    List<ProductVO> listMy();

    void batchRemove(List<Long> productIds);
}
