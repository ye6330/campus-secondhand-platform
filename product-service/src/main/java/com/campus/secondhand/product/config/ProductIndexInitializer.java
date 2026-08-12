package com.campus.secondhand.product.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.product.entity.Product;
import com.campus.secondhand.product.entity.ProductIndex;
import com.campus.secondhand.product.mapper.ProductMapper;
import com.campus.secondhand.product.repository.ProductIndexRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductIndexInitializer.class);
    private final ProductMapper productMapper;
    private final ProductIndexRepository productIndexRepository;

    public ProductIndexInitializer(ProductMapper productMapper, ProductIndexRepository productIndexRepository) {
        this.productMapper = productMapper;
        this.productIndexRepository = productIndexRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, "已上架")
            );
            for (Product product : products) {
                productIndexRepository.save(ProductIndex.from(product));
            }
            log.info("ES索引初始化完成，同步商品数={}", products.size());
        } catch (Exception e) {
            log.warn("ES索引初始化失败", e);
        }
    }
}