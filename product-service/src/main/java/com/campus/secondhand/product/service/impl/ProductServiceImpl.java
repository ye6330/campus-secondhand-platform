package com.campus.secondhand.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.product.dto.CreateProductRequest;
import com.campus.secondhand.product.dto.UpdateProductRequest;
import com.campus.secondhand.product.entity.Product;
import com.campus.secondhand.product.mapper.ProductMapper;
import com.campus.secondhand.product.service.ProductService;
import com.campus.secondhand.product.vo.ProductVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public List<ProductVO> list() {
        List<Product> products = productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, "已上架")
                .orderByDesc(Product::getId)
        );
        return toVOList(products);
    }

    @Override
    public List<ProductVO> listMy(Long userId) {
        List<Product> products = productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getSellerId, userId)
                .orderByDesc(Product::getId)
        );
        return toVOList(products);
    }

    @Override
    public ProductVO getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return toVO(product);
    }

    @Override
    public ProductVO create(CreateProductRequest request) {
        Long currentUserId = UserContext.getUserId();
        String currentUsername = UserContext.getUsername();

        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCoverImage(request.getCoverImage());
        product.setSellerId(currentUserId);
        product.setSellerName(currentUsername);
        product.setStatus("待审核");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return toVO(product);
    }

    @Override
    public ProductVO update(Long id, UpdateProductRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getSellerId().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权修改此商品");
        }

        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCoverImage(request.getCoverImage());
        product.setStatus("待审核");
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return toVO(product);
    }

    @Override
    public void deleteById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getSellerId().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权删除此商品");
        }
        productMapper.deleteById(id);
    }

    @Override
    public List<ProductVO> listPending() {
        List<Product> products = productMapper.selectList(
            new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, "待审核")
                .orderByDesc(Product::getId)
        );
        return toVOList(products);
    }

    @Override
    public void review(Long id, String action) {
        String role = UserContext.getRole();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("无权审核商品");
        }
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"待审核".equals(product.getStatus())) {
            throw new RuntimeException("该商品不在待审核状态");
        }
        if ("approve".equals(action)) {
            product.setStatus("已上架");
        } else if ("reject".equals(action)) {
            product.setStatus("已拒绝");
        } else {
            throw new RuntimeException("审核操作不正确");
        }
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    private List<ProductVO> toVOList(List<Product> products) {
        List<ProductVO> result = new ArrayList<>();
        for (Product product : products) {
            result.add(toVO(product));
        }
        return result;
    }

    private ProductVO toVO(Product entity) {
        ProductVO product = new ProductVO();
        product.setId(entity.getId());
        product.setTitle(entity.getTitle());
        product.setDescription(entity.getDescription());
        product.setPrice(entity.getPrice());
        product.setCoverImage(entity.getCoverImage());
        product.setSellerId(entity.getSellerId());
        product.setSellerName(entity.getSellerName());
        product.setStatus(entity.getStatus());
        if (entity.getCreatedAt() != null) {
            product.setCreatedAt(entity.getCreatedAt().format(TIME_FORMATTER));
        }
        return product;
    }
}
