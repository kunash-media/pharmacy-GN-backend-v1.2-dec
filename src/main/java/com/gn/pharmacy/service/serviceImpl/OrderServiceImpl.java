package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.request.OrderItemDto;
import com.gn.pharmacy.dto.request.OrderRequestDto;
import com.gn.pharmacy.dto.response.OrderResponseDto;
import com.gn.pharmacy.entity.OrderEntity;
import com.gn.pharmacy.entity.OrderItemEntity;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.entity.UserEntity;
import com.gn.pharmacy.repository.OrderItemRepository;
import com.gn.pharmacy.repository.OrderRepository;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.repository.UserRepository;
import com.gn.pharmacy.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }


    @Override
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {

        logger.info("Creating new order");

        OrderEntity orderEntity = new OrderEntity();

        UserEntity user = userRepository.findById(orderRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + orderRequestDto.getUserId()));
        orderEntity.setUser(user);

        orderEntity.setOrderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")));
        mapOrderFields(orderRequestDto, orderEntity);

        OrderEntity savedEntity = orderRepository.save(orderEntity);

        // NEW FIX : Deduct product quantities
        if (orderRequestDto.getOrderItems() != null && !orderRequestDto.getOrderItems().isEmpty()) {
            for (OrderItemDto itemDto : orderRequestDto.getOrderItems()) {
                if (itemDto.getProductId() != null) {
                    ProductEntity product = productRepository.findById(itemDto.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDto.getProductId()));

                    Integer currentStockQuantity = product.getProductQuantity();
                    if (currentStockQuantity < itemDto.getQuantity()) {
                        throw new RuntimeException("Insufficient stock for product ID: " + itemDto.getProductId() +
                                ". Available: " + currentStockQuantity + ", Required: " + itemDto.getQuantity());
                    }

                    Integer newStock = currentStockQuantity - itemDto.getQuantity();
                    product.setProductQuantity(newStock);
                    productRepository.save(product);
                    logger.info("Deducted {} units from product ID: {}. New stock: {}",
                            itemDto.getQuantity(), product.getProductId(), newStock);
                }
            }
            // Existing code continues...
            List<OrderItemEntity> orderItems = orderRequestDto.getOrderItems().stream()
                    .map(itemDto -> createOrderItemEntity(itemDto, savedEntity))
                    .collect(Collectors.toList());

            orderItemRepository.saveAll(orderItems);
            savedEntity.setOrderItems(orderItems);
        }
        logger.info("Order created with ID: {}", savedEntity.getOrderId());
        return mapToResponseDto(savedEntity);
    }


    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        logger.info("Fetching order with ID: {}", orderId);
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        return mapToResponseDto(orderEntity);
    }

    @Override
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        logger.info("Fetching all orders with pagination");
        Page<OrderEntity> orderPage = orderRepository.findAll(pageable);
        return orderPage.map(this::mapToResponseDto);
    }

    @Override
    public OrderResponseDto updateOrder(Long orderId, OrderRequestDto orderRequestDto) {
        logger.info("Updating order with ID: {}", orderId);
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (orderRequestDto.getUserId() != null) {
            UserEntity user = userRepository.findById(orderRequestDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + orderRequestDto.getUserId()));
            orderEntity.setUser(user);
        }
        mapOrderFields(orderRequestDto, orderEntity);

        if (orderRequestDto.getOrderItems() != null) {
            // === FIX: Clear the list first ===
            orderEntity.getOrderItems().clear();

            // Delete old items from DB
            orderItemRepository.deleteAll(orderEntity.getOrderItems()); // safe: list is now empty

            // Create new items
            List<OrderItemEntity> newOrderItems = orderRequestDto.getOrderItems().stream()
                    .map(itemDto -> createOrderItemEntity(itemDto, orderEntity))
                    .collect(Collectors.toList());
            orderItemRepository.saveAll(newOrderItems);
            orderEntity.getOrderItems().addAll(newOrderItems); // re-populate
        }

        OrderEntity updatedEntity = orderRepository.save(orderEntity);
        logger.info("Order updated with ID: {}", updatedEntity.getOrderId());
        return mapToResponseDto(updatedEntity);
    }

    @Override
    public OrderResponseDto patchOrder(Long orderId, OrderRequestDto orderRequestDto) {
        logger.info("Patching order with ID: {}", orderId);
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (orderRequestDto.getUserId() != null) {
            UserEntity user = userRepository.findById(orderRequestDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + orderRequestDto.getUserId()));
            orderEntity.setUser(user);
        }

        if (orderRequestDto.getShippingAddress() != null) orderEntity.setShippingAddress(orderRequestDto.getShippingAddress());
        if (orderRequestDto.getShippingAddress2() != null) orderEntity.setShippingAddress2(orderRequestDto.getShippingAddress2());
        if (orderRequestDto.getShippingCity() != null) orderEntity.setShippingCity(orderRequestDto.getShippingCity());
        if (orderRequestDto.getShippingState() != null) orderEntity.setShippingState(orderRequestDto.getShippingState());
        if (orderRequestDto.getShippingPincode() != null) orderEntity.setShippingPincode(orderRequestDto.getShippingPincode());
        if (orderRequestDto.getShippingCountry() != null) orderEntity.setShippingCountry(orderRequestDto.getShippingCountry());
        if (orderRequestDto.getShippingFirstName() != null) orderEntity.setShippingFirstName(orderRequestDto.getShippingFirstName());
        if (orderRequestDto.getShippingLastName() != null) orderEntity.setShippingLastName(orderRequestDto.getShippingLastName());
        if (orderRequestDto.getShippingEmail() != null) orderEntity.setShippingEmail(orderRequestDto.getShippingEmail());
        if (orderRequestDto.getShippingPhone() != null) orderEntity.setShippingPhone(orderRequestDto.getShippingPhone());
        if (orderRequestDto.getCustomerFirstName() != null) orderEntity.setCustomerFirstName(orderRequestDto.getCustomerFirstName());
        if (orderRequestDto.getCustomerLastName() != null) orderEntity.setCustomerLastName(orderRequestDto.getCustomerLastName());
        if (orderRequestDto.getCustomerPhone() != null) orderEntity.setCustomerPhone(orderRequestDto.getCustomerPhone());
        if (orderRequestDto.getCustomerEmail() != null) orderEntity.setCustomerEmail(orderRequestDto.getCustomerEmail());
        if (orderRequestDto.getPaymentMethod() != null) orderEntity.setPaymentMethod(orderRequestDto.getPaymentMethod());
        if (orderRequestDto.getTotalAmount() != null) orderEntity.setTotalAmount(orderRequestDto.getTotalAmount());
        if (orderRequestDto.getTax() != null) orderEntity.setTax(orderRequestDto.getTax());
        if (orderRequestDto.getCouponApplied() != null) orderEntity.setCouponApplied(orderRequestDto.getCouponApplied());
        if (orderRequestDto.getConvenienceFee() != null) orderEntity.setConvenienceFee(orderRequestDto.getConvenienceFee());
        if (orderRequestDto.getDiscountPercent() != null) orderEntity.setDiscountPercent(orderRequestDto.getDiscountPercent());
        if (orderRequestDto.getDiscountAmount() != null) orderEntity.setDiscountAmount(orderRequestDto.getDiscountAmount());
        if (orderRequestDto.getOrderStatus() != null) orderEntity.setOrderStatus(orderRequestDto.getOrderStatus());
        if (orderRequestDto.getOrderDate() != null) orderEntity.setOrderDate(orderRequestDto.getOrderDate());
        if (orderRequestDto.getDeliveryDate() != null) orderEntity.setDeliveryDate(orderRequestDto.getDeliveryDate());

        if (orderRequestDto.getOrderItems() != null) {
            if (orderEntity.getOrderItems() != null) {
                orderItemRepository.deleteAll(orderEntity.getOrderItems());
            }
            List<OrderItemEntity> newOrderItems = orderRequestDto.getOrderItems().stream()
                    .map(itemDto -> createOrderItemEntity(itemDto, orderEntity))
                    .collect(Collectors.toList());
            orderItemRepository.saveAll(newOrderItems);
            orderEntity.setOrderItems(newOrderItems);
        }

        OrderEntity updatedEntity = orderRepository.save(orderEntity);
        logger.info("Order patched with ID: {}", updatedEntity.getOrderId());
        return mapToResponseDto(updatedEntity);
    }

    @Override
    public void deleteOrder(Long orderId) {
        logger.info("Deleting order with ID: {}", orderId);
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (orderEntity.getOrderItems() != null) {
            orderItemRepository.deleteAll(orderEntity.getOrderItems());
        }

        orderRepository.deleteById(orderId);
        logger.info("Order deleted with ID: {}", orderId);
    }

    private void mapOrderFields(OrderRequestDto requestDto, OrderEntity orderEntity) {
        orderEntity.setShippingAddress(requestDto.getShippingAddress());
        orderEntity.setShippingAddress2(requestDto.getShippingAddress2());
        orderEntity.setShippingCity(requestDto.getShippingCity());
        orderEntity.setShippingState(requestDto.getShippingState());
        orderEntity.setShippingPincode(requestDto.getShippingPincode());
        orderEntity.setShippingCountry(requestDto.getShippingCountry());
        orderEntity.setShippingFirstName(requestDto.getShippingFirstName());
        orderEntity.setShippingLastName(requestDto.getShippingLastName());
        orderEntity.setShippingEmail(requestDto.getShippingEmail());
        orderEntity.setShippingPhone(requestDto.getShippingPhone());
        orderEntity.setCustomerFirstName(requestDto.getCustomerFirstName());
        orderEntity.setCustomerLastName(requestDto.getCustomerLastName());
        orderEntity.setCustomerPhone(requestDto.getCustomerPhone());
        orderEntity.setCustomerEmail(requestDto.getCustomerEmail());
        orderEntity.setPaymentMethod(requestDto.getPaymentMethod());
        orderEntity.setTotalAmount(requestDto.getTotalAmount());
        orderEntity.setTax(requestDto.getTax());
        orderEntity.setCouponApplied(requestDto.getCouponApplied());
        orderEntity.setConvenienceFee(requestDto.getConvenienceFee());
        orderEntity.setDiscountPercent(requestDto.getDiscountPercent());
        orderEntity.setDiscountAmount(requestDto.getDiscountAmount());
        orderEntity.setOrderStatus(requestDto.getOrderStatus());
        orderEntity.setDeliveryDate(requestDto.getDeliveryDate());
    }

    private OrderItemEntity createOrderItemEntity(OrderItemDto itemDto, OrderEntity orderEntity) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrder(orderEntity);

        // FIX: Use the values from the DTO instead of fetching from database
        // This preserves the prices at the time of order placement
        if (itemDto.getProductId() != null) {
            ProductEntity product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDto.getProductId()));
            orderItemEntity.setProduct(product);
        }

        // Always use the values from the request DTO
        orderItemEntity.setItemName(itemDto.getItemName());
        orderItemEntity.setItemPrice(itemDto.getItemPrice());
        orderItemEntity.setItemOldPrice(itemDto.getItemOldPrice());
        orderItemEntity.setQuantity(itemDto.getQuantity());
        orderItemEntity.setSubtotal(itemDto.getSubtotal());

        return orderItemEntity;
    }

    private OrderResponseDto mapToResponseDto(OrderEntity orderEntity) {
        OrderResponseDto responseDto = new OrderResponseDto();
        responseDto.setOrderId(orderEntity.getOrderId());
        responseDto.setUserId(orderEntity.getUser() != null ? orderEntity.getUser().getUserId() : null);
        responseDto.setShippingAddress(orderEntity.getShippingAddress());
        responseDto.setShippingAddress2(orderEntity.getShippingAddress2());
        responseDto.setShippingCity(orderEntity.getShippingCity());
        responseDto.setShippingState(orderEntity.getShippingState());
        responseDto.setShippingPincode(orderEntity.getShippingPincode());
        responseDto.setShippingCountry(orderEntity.getShippingCountry());
        responseDto.setShippingFirstName(orderEntity.getShippingFirstName());
        responseDto.setShippingLastName(orderEntity.getShippingLastName());
        responseDto.setShippingEmail(orderEntity.getShippingEmail());
        responseDto.setShippingPhone(orderEntity.getShippingPhone());
        responseDto.setCustomerFirstName(orderEntity.getCustomerFirstName());
        responseDto.setCustomerLastName(orderEntity.getCustomerLastName());
        responseDto.setCustomerPhone(orderEntity.getCustomerPhone());
        responseDto.setCustomerEmail(orderEntity.getCustomerEmail());
        responseDto.setPaymentMethod(orderEntity.getPaymentMethod());
        responseDto.setTotalAmount(orderEntity.getTotalAmount());
        responseDto.setTax(orderEntity.getTax());
        responseDto.setCouponApplied(orderEntity.getCouponApplied());
        responseDto.setConvenienceFee(orderEntity.getConvenienceFee());
        responseDto.setDiscountPercent(orderEntity.getDiscountPercent());
        responseDto.setDiscountAmount(orderEntity.getDiscountAmount());
        responseDto.setOrderStatus(orderEntity.getOrderStatus());
        responseDto.setOrderDate(orderEntity.getOrderDate());
        responseDto.setDeliveryDate(orderEntity.getDeliveryDate());

        if (orderEntity.getOrderItems() != null) {
            List<OrderItemDto> orderItemDtos = orderEntity.getOrderItems().stream()
                    .map(item -> {
                        OrderItemDto itemDto = new OrderItemDto();
                        itemDto.setOrderItemId(item.getOrderItemId());
                        itemDto.setProductId(item.getProduct() != null ? item.getProduct().getProductId() : null);
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setItemPrice(item.getItemPrice());
                        itemDto.setItemOldPrice(item.getItemOldPrice());
                        itemDto.setSubtotal(item.getSubtotal());
                        itemDto.setItemName(item.getItemName());
                        return itemDto;
                    })
                    .collect(Collectors.toList());
            responseDto.setOrderItems(orderItemDtos);
        }

        return responseDto;
    }


    //=============== Cancel Order with Restore Product Quantity ================//

    @Override
    public OrderResponseDto cancelOrder(Long orderId) {
        logger.info("Cancelling order with ID: {}", orderId);

        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Check if order can be cancelled
        if ("CANCELLED".equals(orderEntity.getOrderStatus())) {
            throw new RuntimeException("Order is already cancelled");
        }

        if ("DELIVERED".equals(orderEntity.getOrderStatus())) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        // Restore product quantities
        if (orderEntity.getOrderItems() != null) {

            for (OrderItemEntity item : orderEntity.getOrderItems()) {
                if (item.getProduct() != null) {

                    ProductEntity product = item.getProduct();
                    Integer currentStockQuantity = product.getProductQuantity();
                    Integer restoredStock = currentStockQuantity + item.getQuantity();
                    product.setProductQuantity(restoredStock);

                    productRepository.save(product);
                    logger.info("Restored {} units to product ID: {}. New stock: {}",
                            item.getQuantity(), product.getProductId(), restoredStock);
                }
            }
        }

        // Update order status to CANCELLED
        orderEntity.setOrderStatus("CANCELLED");
        OrderEntity cancelledOrder = orderRepository.save(orderEntity);

        logger.info("Order cancelled successfully with ID: {}", orderId);
        return mapToResponseDto(cancelledOrder);
    }




}