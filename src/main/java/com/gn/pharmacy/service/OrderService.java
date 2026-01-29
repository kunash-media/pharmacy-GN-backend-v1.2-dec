package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.request.ExchangeRequestDto;
import com.gn.pharmacy.dto.request.OrderRequestDto;
import com.gn.pharmacy.dto.response.OrderResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);
    OrderResponseDto getOrderById(Long orderId);
    Page<OrderResponseDto> getAllOrders(Pageable pageable);
    OrderResponseDto updateOrder(Long orderId, OrderRequestDto orderRequestDto);
    OrderResponseDto patchOrder(Long orderId, OrderRequestDto orderRequestDto);
    void deleteOrder(Long orderId);

    OrderResponseDto cancelOrder(Long orderId);

    Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable);

    OrderResponseDto requestExchange(Long orderItemId, ExchangeRequestDto exchangeRequestDto);
}

