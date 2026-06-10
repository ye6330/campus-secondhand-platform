package com.campus.secondhand.order.service;

import com.campus.secondhand.order.dto.CreateOrderRequest;
import com.campus.secondhand.order.dto.HandleOrderRequest;
import com.campus.secondhand.order.vo.OrderVO;
import java.util.List;

public interface OrderService {

    OrderVO create(CreateOrderRequest request);

    List<OrderVO> listMyBuy(String status);

    List<OrderVO> listMySell(String status);

    List<OrderVO> listAdmin(String status);

    void confirm(Long id, HandleOrderRequest request);

    void reject(Long id, HandleOrderRequest request);

    void cancel(Long id);
}
