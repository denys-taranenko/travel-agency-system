package com.epam.agency.service;

import com.epam.agency.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    void createOrder(OrderDTO orderDTO);

    void approveRequest(String orderId);

    void cancelRequest(String orderId);

    OrderDTO getOrderById(String orderId);

    void cancelOrder(String orderId, String reason);

    void payOrder(String orderId, String stripeToken);

    void deleteOrder(String orderId);

    Page<OrderDTO> getAllOrders(String status, Pageable pageable);

    List<OrderDTO> getAllUserOrders(String userId);
}
