package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.request.ExchangeRequestDto;
import com.gn.pharmacy.dto.request.OrderRequestDto;
import com.gn.pharmacy.dto.response.OrderResponseDto;
import com.gn.pharmacy.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final Logger logger = LoggerFactory.getLogger(OrderController.class);

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        return ResponseEntity.ok(orderService.createOrder(orderRequestDto));
    }

    @GetMapping("get-by-order-id/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping("/get-all-orders")
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @PutMapping("/update-by-order-id/{orderId}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long orderId,
            @RequestBody OrderRequestDto orderRequestDto) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, orderRequestDto));
    }

    @PatchMapping("/patch-by-order-id/{orderId}")
    public ResponseEntity<OrderResponseDto> patchOrder(@PathVariable Long orderId,
                                                       @RequestBody OrderRequestDto orderRequestDto) {
        return ResponseEntity.ok(orderService.patchOrder(orderId, orderRequestDto));
    }

    @DeleteMapping("/delete-by-order-id/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/cancel-order/{orderId}")
    public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long orderId) {
        logger.info("Received cancel order request for order ID: {}", orderId);
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }

    // === ADD THIS METHOD TO OrderController.java ===
    @GetMapping("/get-order-by-userId/{userId}")
    public ResponseEntity<Page<OrderResponseDto>> getOrdersByUserId(
            @PathVariable Long userId,
            Pageable pageable) {
        logger.info("Fetching orders for user ID: {}", userId);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageable));
    }


    // Add to OrderController.java
    @PostMapping("/request-exchange/{orderItemId}")
    public ResponseEntity<OrderResponseDto> requestExchange(
            @PathVariable Long orderItemId,
            @RequestBody ExchangeRequestDto exchangeRequestDto) {
        logger.info("Received exchange request for order item ID: {}", orderItemId);
        return ResponseEntity.ok(orderService.requestExchange(orderItemId, exchangeRequestDto));
    }
}