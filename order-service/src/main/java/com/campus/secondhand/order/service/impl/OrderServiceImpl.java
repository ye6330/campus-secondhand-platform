package com.campus.secondhand.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.core.constants.MQConstants;
import com.campus.secondhand.common.core.dto.NotificationMessage;
import com.campus.secondhand.common.core.log.OperationLog;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.order.client.ProductClient;
import com.campus.secondhand.order.client.UserClient;
import com.campus.secondhand.order.dto.CreateOrderRequest;
import com.campus.secondhand.order.dto.HandleOrderRequest;
import com.campus.secondhand.order.entity.Order;
import com.campus.secondhand.order.mapper.OrderMapper;
import com.campus.secondhand.order.service.OrderService;
import com.campus.secondhand.order.vo.OrderVO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final OrderMapper orderMapper;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    public OrderServiceImpl(OrderMapper orderMapper, ProductClient productClient,
        UserClient userClient, RabbitTemplate rabbitTemplate) {
        this.orderMapper = orderMapper;
        this.productClient = productClient;
        this.userClient = userClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public OrderVO create(CreateOrderRequest request) {
        Long buyerId = UserContext.getUserId();
        String buyerName = UserContext.getUsername();
        if (buyerId == null || buyerName == null) {
            throw new RuntimeException("未登录");
        }
        Map<String, Object> response = productClient.detail(request.getProductId());
        if (!isSuccess(response) || response.get("data") == null) {
            throw new RuntimeException("商品不存在");
        }
        Map<String, Object> product = castMap(response.get("data"));
        Long sellerId = toLong(product.get("sellerId"));
        String status = String.valueOf(product.get("status"));
        if (sellerId == null || buyerId.equals(sellerId)) {
            throw new RuntimeException("不能购买自己的商品");
        }
        if (!"已上架".equals(status)) {
            throw new RuntimeException("当前商品不可下单");
        }
        Long pendingCount = orderMapper.selectCount(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getProductId, request.getProductId())
                .in(Order::getStatus, "待确认", "已确认")
        );
        if (pendingCount != null && pendingCount > 0) {
            throw new RuntimeException("该商品正在交易中，暂时无法购买");
        }
        Map<String, Object> buyerContact = getContact(buyerId);
        Map<String, Object> sellerContact = getContact(sellerId);
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProductId(request.getProductId());
        order.setProductTitle(String.valueOf(product.get("title")));
        order.setProductCoverImage(String.valueOf(product.get("coverImage")));
        order.setProductPrice(new BigDecimal(String.valueOf(product.get("price"))));
        order.setBuyerId(buyerId);
        order.setBuyerName(buyerName);
        order.setBuyerPhone(String.valueOf(buyerContact.get("phone")));
        order.setSellerId(sellerId);
        order.setSellerName(String.valueOf(product.get("sellerName")));
        order.setSellerPhone(String.valueOf(sellerContact.get("phone")));
        order.setStatus("待确认");
        order.setNote(request.getNote() == null ? null : request.getNote().trim());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);
        Map<String, Object> tradingResponse = productClient.markTrading(order.getProductId());
        if (!isSuccess(tradingResponse)) {
            orderMapper.deleteById(order.getId());
            throw new RuntimeException(tradingResponse == null || tradingResponse.get("message") == null
                ? "商品状态更新失败"
                : String.valueOf(tradingResponse.get("message")));
        }
        notifyUser(
            sellerId,
            "新的购买意向",
            "用户“" + buyerName + "”想购买你的商品《" + order.getProductTitle() + "》，请前往我的订单处理。"
        );
        return toVO(order);
    }

    @Override
    public List<OrderVO> listMyBuy(String status) {
        return listByRole(Order::getBuyerId, UserContext.getUserId(), status);
    }

    @Override
    public List<OrderVO> listMySell(String status) {
        return listByRole(Order::getSellerId, UserContext.getUserId(), status);
    }

    @Override
    public List<OrderVO> listAdmin(String status) {
        if (!"ADMIN".equals(UserContext.getRole())) {
            throw new RuntimeException("无权查看订单");
        }
        return listOrders(status);
    }

    @Override
    @OperationLog("卖家确认订单")
    public void confirm(Long id, HandleOrderRequest request) {
        Order order = getOrder(id);
        Long userId = UserContext.getUserId();
        if (!order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        if (!"待确认".equals(order.getStatus())) {
            throw new RuntimeException("该订单当前不可确认");
        }
        if (request == null || request.getTradeImage() == null || request.getTradeImage().trim().isEmpty()) {
            throw new RuntimeException("请上传现场交易图");
        }
        Map<String, Object> response = productClient.markSold(order.getProductId());
        if (!isSuccess(response)) {
            throw new RuntimeException(response == null || response.get("message") == null
                ? "商品状态更新失败"
                : String.valueOf(response.get("message")));
        }
        order.setStatus("已确认");
        order.setNote(request == null || request.getNote() == null || request.getNote().trim().isEmpty()
            ? order.getNote()
            : request.getNote().trim());
        order.setTradeImage(request.getTradeImage().trim());
        order.setTradeConfirmedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        List<Order> others = orderMapper.selectList(
            new LambdaQueryWrapper<Order>()
                .eq(Order::getProductId, order.getProductId())
                .eq(Order::getStatus, "待确认")
                .ne(Order::getId, order.getId())
        );
        for (Order other : others) {
            other.setStatus("已拒绝");
            other.setNote("商品已售出");
            other.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(other);
            notifyUser(
                other.getBuyerId(),
                "购买意向处理结果",
                "你对商品《" + other.getProductTitle() + "》的购买意向未成功，原因：商品已售出。"
            );
        }
        notifyUser(
            order.getBuyerId(),
            "购买意向处理结果",
            "卖家已确认你的购买意向，商品《" + order.getProductTitle() + "》已成交。"
        );
    }

    @Override
    @OperationLog("卖家拒绝订单")
    public void reject(Long id, HandleOrderRequest request) {
        Order order = getOrder(id);
        Long userId = UserContext.getUserId();
        if (!order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        if (!"待确认".equals(order.getStatus())) {
            throw new RuntimeException("该订单当前不可拒绝");
        }
        order.setStatus("已拒绝");
        order.setNote(request == null || request.getNote() == null ? order.getNote() : request.getNote().trim());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        Map<String, Object> response = productClient.restoreOnShelf(order.getProductId());
        if (!isSuccess(response)) {
            throw new RuntimeException(response == null || response.get("message") == null
                ? "商品状态恢复失败"
                : String.valueOf(response.get("message")));
        }
        notifyUser(
            order.getBuyerId(),
            "购买意向处理结果",
            "卖家已拒绝你对商品《" + order.getProductTitle() + "》的购买意向。"
                + (order.getNote() == null || order.getNote().isEmpty() ? "" : " 说明：" + order.getNote())
        );
    }

    @Override
    @OperationLog("买家取消订单")
    public void cancel(Long id) {
        Order order = getOrder(id);
        Long userId = UserContext.getUserId();
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权操作该订单");
        }
        if (!"待确认".equals(order.getStatus())) {
            throw new RuntimeException("该订单当前不可取消");
        }
        order.setStatus("已取消");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        Map<String, Object> response = productClient.restoreOnShelf(order.getProductId());
        if (!isSuccess(response)) {
            throw new RuntimeException(response == null || response.get("message") == null
                ? "商品状态恢复失败"
                : String.valueOf(response.get("message")));
        }
        notifyUser(
            order.getSellerId(),
            "购买意向已取消",
            "买家已取消商品《" + order.getProductTitle() + "》的购买意向。"
        );
    }

    private List<OrderVO> listByRole(com.baomidou.mybatisplus.core.toolkit.support.SFunction<Order, Long> column,
        Long userId, String status) {
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
            .eq(column, userId)
            .orderByDesc(Order::getCreatedAt);
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Order::getStatus, status.trim());
        }
        return orderMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    private List<OrderVO> listOrders(String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
            .orderByDesc(Order::getCreatedAt);
        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq(Order::getStatus, status.trim());
        }
        return orderMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    private Order getOrder(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    private OrderVO toVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setProductId(order.getProductId());
        vo.setProductTitle(order.getProductTitle());
        vo.setProductCoverImage(order.getProductCoverImage());
        vo.setProductPrice(order.getProductPrice());
        vo.setBuyerId(order.getBuyerId());
        vo.setBuyerName(order.getBuyerName());
        vo.setBuyerPhone(order.getBuyerPhone());
        vo.setSellerId(order.getSellerId());
        vo.setSellerName(order.getSellerName());
        vo.setSellerPhone(order.getSellerPhone());
        vo.setStatus(order.getStatus());
        vo.setNote(order.getNote());
        vo.setTradeImage(order.getTradeImage());
        if (order.getCreatedAt() != null) {
            vo.setCreatedAt(order.getCreatedAt().format(TIME_FORMATTER));
        }
        if (order.getTradeConfirmedAt() != null) {
            vo.setTradeConfirmedAt(order.getTradeConfirmedAt().format(TIME_FORMATTER));
        }
        return vo;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    private boolean isSuccess(Map<String, Object> response) {
        return response != null && Integer.valueOf(200).equals(response.get("code"));
    }

    private Map<String, Object> getContact(Long userId) {
        Map<String, Object> response = userClient.contact(userId);
        if (!isSuccess(response) || response.get("data") == null) {
            throw new RuntimeException("用户联系方式获取失败");
        }
        return castMap(response.get("data"));
    }

    private void notifyUser(Long userId, String title, String content) {
        try {
            NotificationMessage message = new NotificationMessage(userId, title, content);
            rabbitTemplate.convertAndSend(MQConstants.NOTIFICATION_EXCHANGE,
                MQConstants.NOTIFICATION_ROUTING_KEY, message);
        } catch (Exception e) {
            throw new RuntimeException("通知发送失败", e);
        }
    }
}
