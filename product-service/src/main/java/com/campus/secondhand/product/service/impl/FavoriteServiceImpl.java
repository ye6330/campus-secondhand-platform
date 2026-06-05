package com.campus.secondhand.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.product.entity.Favorite;
import com.campus.secondhand.product.entity.Product;
import com.campus.secondhand.product.mapper.FavoriteMapper;
import com.campus.secondhand.product.mapper.ProductMapper;
import com.campus.secondhand.product.service.FavoriteService;
import com.campus.secondhand.product.vo.ProductVO;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper, ProductMapper productMapper) {
        this.favoriteMapper = favoriteMapper;
        this.productMapper = productMapper;
    }

    @Override
    public void add(Long productId) {
        Long userId = UserContext.getUserId();
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (favoriteMapper.exists(userId, productId) > 0) {
            throw new RuntimeException("已经收藏过了");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        fav.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(fav);
    }

    @Override
    public void remove(Long productId) {
        Long userId = UserContext.getUserId();
        favoriteMapper.delete(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId)
        );
    }

    @Override
    public List<ProductVO> listMy() {
        Long userId = UserContext.getUserId();
        List<Favorite> favs = favoriteMapper.selectList(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt)
        );
        List<ProductVO> result = new ArrayList<>();
        for (Favorite fav : favs) {
            Product product = productMapper.selectById(fav.getProductId());
            if (product != null) {
                ProductVO vo = toVO(product);
                vo.setFavoriteCount(favoriteMapper.countByProductId(product.getId()));
                vo.setFavorited(true);
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public void batchRemove(List<Long> productIds) {
        Long userId = UserContext.getUserId();
        favoriteMapper.delete(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .in(Favorite::getProductId, productIds)
        );
    }

    private ProductVO toVO(Product entity) {
        ProductVO vo = new ProductVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setDescription(entity.getDescription());
        vo.setPrice(entity.getPrice());
        vo.setCoverImage(entity.getCoverImage());
        vo.setSellerId(entity.getSellerId());
        vo.setSellerName(entity.getSellerName());
        vo.setStatus(entity.getStatus());
        if (entity.getCreatedAt() != null) {
            vo.setCreatedAt(entity.getCreatedAt().format(TIME_FORMATTER));
        }
        return vo;
    }
}
