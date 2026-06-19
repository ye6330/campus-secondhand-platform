package com.campus.secondhand.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.product.dto.CreateProductRequest;
import com.campus.secondhand.product.dto.UpdateProductRequest;
import com.campus.secondhand.product.entity.Product;
import com.campus.secondhand.product.mapper.FavoriteMapper;
import com.campus.secondhand.product.mapper.ProductMapper;
import com.campus.secondhand.product.service.ProductService;
import com.campus.secondhand.product.vo.ProductVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ProductMapper productMapper;
    private final FavoriteMapper favoriteMapper;
    private final Cache productCache;

    public ProductServiceImpl(ProductMapper productMapper, FavoriteMapper favoriteMapper,
        CacheManager cacheManager) {
        this.productMapper = productMapper;
        this.favoriteMapper = favoriteMapper;
        this.productCache = cacheManager.getCache("products");
    }

    @Override
    public List<ProductVO> list(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getStatus, "已上架");
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Product::getTitle, keyword)
                .or()
                .like(Product::getDescription, keyword)
            );
        }
        wrapper.orderByDesc(Product::getId);
        return toVOList(productMapper.selectList(wrapper));
    }

    @Override
    public List<String> suggest(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .select(Product::getTitle)
            .eq(Product::getStatus, "已上架")
            .like(Product::getTitle, keyword)
            .last("LIMIT 8");
        List<Product> products = productMapper.selectList(wrapper);
        List<String> titles = new ArrayList<>();
        for (Product p : products) {
            if (!titles.contains(p.getTitle())) {
                titles.add(p.getTitle());
            }
        }
        return titles;
    }

    @Override
    public List<ProductVO> listMy(Long userId, String status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getSellerId, userId)
            .orderByDesc(Product::getId);
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Product::getStatus, status.trim());
        }
        List<Product> products = productMapper.selectList(wrapper);
        return toVOList(products);
    }

    @Override
    public List<ProductVO> listBySeller(Long sellerId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getSellerId, sellerId)
            .eq(Product::getStatus, "已上架")
            .orderByDesc(Product::getId);
        return toVOList(productMapper.selectList(wrapper));
    }

    @Override
    public ProductVO getById(Long id) {
        Product product = productCache.get(id, Product.class);
        if (product == null) {
            product = productMapper.selectById(id);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            productCache.put(id, product);
        }
        if ("已上架".equals(product.getStatus())) {
            int nextViewCount = product.getViewCount() == null ? 1 : product.getViewCount() + 1;
            product.setViewCount(nextViewCount);
            productMapper.updateById(product);
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
        product.setViewCount(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return toVO(product);
    }

    @Override
    public ProductVO update(Long id, UpdateProductRequest request) {
        productCache.evict(id);
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
        productCache.evict(id);
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
    public void offShelf(Long id) {
        productCache.evict(id);
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getSellerId().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权操作此商品");
        }
        if (!"已上架".equals(product.getStatus())) {
            throw new RuntimeException("只有已上架商品才能下架");
        }
        product.setStatus("已下架");
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public void relist(Long id) {
        productCache.evict(id);
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getSellerId().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权操作此商品");
        }
        if (!"已下架".equals(product.getStatus())) {
            throw new RuntimeException("只有已下架商品才能重新上架");
        }
        product.setStatus("待审核");
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public void markSold(Long id) {
        productCache.evict(id);
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!product.getSellerId().equals(UserContext.getUserId())) {
            throw new RuntimeException("无权操作此商品");
        }
        if (!"已上架".equals(product.getStatus())) {
            throw new RuntimeException("只有已上架商品才能标记为已售出");
        }
        product.setStatus("已售出");
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
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
        productCache.evict(id);
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
        product.setFavoriteCount(favoriteMapper.countByProductId(entity.getId()));
        product.setViewCount(entity.getViewCount() == null ? 0 : entity.getViewCount());
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null) {
            product.setFavorited(favoriteMapper.exists(currentUserId, entity.getId()) > 0);
        }
        if (entity.getCreatedAt() != null) {
            product.setCreatedAt(entity.getCreatedAt().format(TIME_FORMATTER));
        }
        return product;
    }
}
