package com.campus.secondhand.product.repository;

import com.campus.secondhand.product.entity.ProductIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductIndexRepository extends ElasticsearchRepository<ProductIndex, Long> {
}