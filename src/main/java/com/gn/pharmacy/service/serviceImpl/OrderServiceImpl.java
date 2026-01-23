package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.request.OrderItemDto;
import com.gn.pharmacy.dto.request.OrderRequestDto;
import com.gn.pharmacy.dto.response.OrderResponseDto;
import com.gn.pharmacy.entity.*;

import com.gn.pharmacy.repository.*;

import com.gn.pharmacy.service.OrderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final MbPRepository mbpRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            ProductRepository productRepository, UserRepository userRepository, MbPRepository mbpRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.mbpRepository = mbpRepository;
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {

        logger.info("Creating new order for userId: {}", orderRequestDto.getUserId());

        // Validate user
        UserEntity user = userRepository.findById(orderRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + orderRequestDto.getUserId()));

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUser(user);
        orderEntity.setOrderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")));
        mapOrderFields(orderRequestDto, orderEntity);

        OrderEntity savedEntity = orderRepository.save(orderEntity);

        if (orderRequestDto.getOrderItems() != null && !orderRequestDto.getOrderItems().isEmpty()) {

            for (OrderItemDto itemDto : orderRequestDto.getOrderItems()) {

                if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                    throw new RuntimeException("Invalid quantity for item: " + itemDto.getItemName());
                }

                Long productId = itemDto.getProductId();
                Long mbpId = itemDto.getMbpId();

                if ((productId == null && mbpId == null) || (productId != null && mbpId != null)) {
                    throw new RuntimeException(
                            "Invalid order item: exactly one of 'productId' or 'mbpId' must be provided. " +
                                    "Received productId=" + productId + ", mbpId=" + mbpId +
                                    ", itemName=" + itemDto.getItemName()
                    );
                }

                List<InventoryEntity> batches;
                Object parentEntity;

                if (productId != null) {
                    ProductEntity product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
                    batches = product.getInventoryBatches();
                    parentEntity = product;
                } else {
                    MbPEntity mbp = mbpRepository.findById(mbpId)
                            .orElseThrow(() -> new RuntimeException("MbP product not found with ID: " + mbpId));
                    batches = mbp.getInventoryBatches();
                    parentEntity = mbp;
                }

                if (batches == null || batches.isEmpty()) {
                    throw new RuntimeException("No inventory batches found for item: " + itemDto.getItemName());
                }

                String requestedSize = itemDto.getSize();
                int remainingToDeduct = itemDto.getQuantity();

                boolean deducted = false;

                for (InventoryEntity batch : batches) {
                    if (remainingToDeduct <= 0) break;

                    List<BatchVariant> variants = batch.getVariants();
                    if (variants == null || variants.isEmpty()) continue;

                    // Find variant matching the requested size (case-insensitive, allow null for no-size)
                    BatchVariant targetVariant = variants.stream()
                            .filter(v -> {
                                String variantSize = v.getSize();
                                if (requestedSize == null || requestedSize.trim().isEmpty()) {
                                    return variantSize == null || variantSize.trim().isEmpty();
                                }
                                return requestedSize.equalsIgnoreCase(variantSize);
                            })
                            .findFirst()
                            .orElse(null);

                    if (targetVariant != null && targetVariant.getQuantity() != null && targetVariant.getQuantity() > 0) {
                        int deduct = Math.min(targetVariant.getQuantity(), remainingToDeduct);
                        targetVariant.setQuantity(targetVariant.getQuantity() - deduct);
                        remainingToDeduct -= deduct;
                        deducted = true;

                        logger.info("Deducted {} from batch {} variant size '{}' (parent ID: {})",
                                deduct, batch.getBatchNo(), targetVariant.getSize(),
                                productId != null ? productId : mbpId);
                    }
                }

                if (remainingToDeduct > 0) {
                    throw new RuntimeException(String.format(
                            "Insufficient stock for size '%s'. Required: %d, Available: not enough (item: %s)",
                            requestedSize != null ? requestedSize : "default",
                            itemDto.getQuantity(), itemDto.getItemName()
                    ));
                }

                // Save parent to persist variant changes
                if (parentEntity instanceof ProductEntity) {
                    productRepository.save((ProductEntity) parentEntity);
                } else if (parentEntity instanceof MbPEntity) {
                    mbpRepository.save((MbPEntity) parentEntity);
                }

                // Create order item and store ordered size
                OrderItemEntity orderItem = createOrderItemEntity(itemDto, savedEntity, parentEntity);
                orderItem.setSize(requestedSize);
                savedEntity.getOrderItems().add(orderItem);
            }

            savedEntity = orderRepository.save(savedEntity);
        } else {
            logger.warn("Order created with no items for userId: {}", orderRequestDto.getUserId());
        }

        logger.info("Order created successfully with ID: {}", savedEntity.getOrderId());
        return mapToResponseDto(savedEntity);
    }




    // === ADD THIS METHOD TO OrderServiceImpl.java ===
    @Override
    public Page<OrderResponseDto> getOrdersByUserId(Long userId, Pageable pageable) {
        logger.info("Fetching paginated orders for user ID: {}", userId);

        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Page<OrderEntity> orderPage = orderRepository.findByUser_UserId(userId, pageable);
        return orderPage.map(this::mapToResponseDto);
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
            // === Same as in patchOrder ===
            if (orderEntity.getOrderItems() != null && !orderEntity.getOrderItems().isEmpty()) {
                orderItemRepository.deleteAll(orderEntity.getOrderItems());
                orderEntity.getOrderItems().clear();
            }

            for (OrderItemDto itemDto : orderRequestDto.getOrderItems()) {
                Long productId = itemDto.getProductId();
                Long mbpId = itemDto.getMbpId();

                if ((productId == null && mbpId == null) || (productId != null && mbpId != null)) {
                    throw new RuntimeException(
                            "Invalid order item in update: exactly one of 'productId' or 'mbpId' must be provided. " +
                                    "itemName=" + itemDto.getItemName()
                    );
                }

                Object parentEntity = null;

                if (productId != null) {
                    parentEntity = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
                } else {
                    parentEntity = mbpRepository.findById(mbpId)
                            .orElseThrow(() -> new RuntimeException("MbP product not found with ID: " + mbpId));
                }

                OrderItemEntity newItem = createOrderItemEntity(itemDto, orderEntity, parentEntity);
                orderEntity.getOrderItems().add(newItem);
            }
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
            if (orderEntity.getOrderItems() != null && !orderEntity.getOrderItems().isEmpty()) {
                orderItemRepository.deleteAll(orderEntity.getOrderItems());
                orderEntity.getOrderItems().clear();
            }

            for (OrderItemDto itemDto : orderRequestDto.getOrderItems()) {
                Long productId = itemDto.getProductId();
                Long mbpId = itemDto.getMbpId();

                if ((productId == null && mbpId == null) || (productId != null && mbpId != null)) {
                    throw new RuntimeException(
                            "Invalid order item in update: exactly one of 'productId' or 'mbpId' must be provided. " +
                                    "itemName=" + itemDto.getItemName()
                    );
                }

                Object parentEntity = null;

                if (productId != null) {
                    parentEntity = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
                } else {
                    parentEntity = mbpRepository.findById(mbpId)
                            .orElseThrow(() -> new RuntimeException("MbP product not found with ID: " + mbpId));
                }

                OrderItemEntity newItem = createOrderItemEntity(itemDto, orderEntity, parentEntity);
                orderEntity.getOrderItems().add(newItem);
            }
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

    private OrderItemEntity createOrderItemEntity(OrderItemDto dto, OrderEntity order, Object parentEntity) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setOrder(order);
        entity.setQuantity(dto.getQuantity());
        entity.setItemPrice(dto.getItemPrice());
        entity.setItemOldPrice(dto.getItemOldPrice());
        entity.setSubtotal(dto.getSubtotal());
        entity.setItemName(dto.getItemName());

        // Set the correct reference using the passed parentEntity
        if (parentEntity instanceof ProductEntity) {
            entity.setProduct((ProductEntity) parentEntity);
            entity.setMbP(null);
        } else if (parentEntity instanceof MbPEntity) {
            entity.setMbP((MbPEntity) parentEntity);
            entity.setProduct(null);
        } else {
            throw new RuntimeException("Invalid parent entity type for order item");
        }

        return entity;
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

        // Map Order Items with support for both ProductEntity and MbPEntity
        if (orderEntity.getOrderItems() != null && !orderEntity.getOrderItems().isEmpty()) {
            List<OrderItemDto> orderItemDtos = orderEntity.getOrderItems().stream()
                    .map(item -> {
                        OrderItemDto itemDto = new OrderItemDto();

                        itemDto.setOrderItemId(item.getOrderItemId());

                        // Set correct IDs
                        itemDto.setProductId(item.getProduct() != null ? item.getProduct().getProductId() : null);
                        itemDto.setMbpId(item.getMbP() != null ? item.getMbP().getId() : null);

                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setItemPrice(item.getItemPrice());
                        itemDto.setItemOldPrice(item.getItemOldPrice());
                        itemDto.setSubtotal(item.getSubtotal());
                        itemDto.setItemName(item.getItemName());
                        itemDto.setSize(item.getSize());

                        // === MAIN IMAGE URL LOGIC ===
                        String mainImageUrl = null;

                        if (item.getProduct() != null && item.getProduct().getProductId() != null) {
                            mainImageUrl = "/api/products/" + item.getProduct().getProductId() + "/image";
                        } else if (item.getMbP() != null && item.getMbP().getId() != null) {
                            mainImageUrl = "/api/mb/products/" + item.getMbP().getId() + "/image";
                        }

                        itemDto.setProductMainImage(mainImageUrl);

                        return itemDto;
                    })
                    .collect(Collectors.toList());

            responseDto.setOrderItems(orderItemDtos);
        } else {
            responseDto.setOrderItems(new ArrayList<>());
        }

        return responseDto;
    }




    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        logger.info("Cancelling order with ID: {}", orderId);

        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if ("CANCELLED".equals(orderEntity.getOrderStatus())) {
            throw new RuntimeException("Order is already cancelled");
        }
        if ("DELIVERED".equals(orderEntity.getOrderStatus())) {
            throw new RuntimeException("Cannot cancel delivered order");
        }

        // Restore stock to correct variant
        if (orderEntity.getOrderItems() != null && !orderEntity.getOrderItems().isEmpty()) {
            for (OrderItemEntity item : orderEntity.getOrderItems()) {

                ProductEntity product = item.getProduct();
                MbPEntity mbp = item.getMbP();
                String orderedSize = item.getSize();

                if (product == null && mbp == null) continue;

                List<InventoryEntity> batches = product != null
                        ? product.getInventoryBatches()
                        : mbp.getInventoryBatches();

                int quantityToRestore = item.getQuantity();
                if (quantityToRestore <= 0) continue;

                boolean restored = false;

                // Try to find and restore to existing variant in any batch
                for (InventoryEntity batch : batches) {
                    if (quantityToRestore <= 0) break;

                    List<BatchVariant> variants = batch.getVariants();
                    if (variants == null) continue;

                    BatchVariant targetVariant = variants.stream()
                            .filter(v -> {
                                String variantSize = v.getSize();
                                if (orderedSize == null || orderedSize.trim().isEmpty()) {
                                    return variantSize == null || variantSize.trim().isEmpty();
                                }
                                return orderedSize.equalsIgnoreCase(variantSize);
                            })
                            .findFirst()
                            .orElse(null);

                    if (targetVariant != null) {
                        targetVariant.setQuantity(targetVariant.getQuantity() + quantityToRestore);
                        quantityToRestore = 0;
                        restored = true;

                        logger.info("Restored {} to batch {} variant size '{}' (parent ID: {})",
                                item.getQuantity(), batch.getBatchNo(), targetVariant.getSize(),
                                product != null ? product.getProductId() : mbp.getId());
                    }
                }

                // If no matching variant found → create new return batch with variant
                if (quantityToRestore > 0) {
                    InventoryEntity returnBatch = new InventoryEntity();
                    returnBatch.setBatchNo("RETURN-" + orderId + "-" + item.getOrderItemId());
                    returnBatch.setStockStatus("AVAILABLE");
                    returnBatch.setLastUpdated(LocalDateTime.now());

                    BatchVariant returnVariant = new BatchVariant();
                    returnVariant.setSize(orderedSize);
                    returnVariant.setQuantity(quantityToRestore);
                    returnVariant.setMfgDate("N/A");
                    returnVariant.setExpDate("N/A");

                    returnBatch.getVariants().add(returnVariant);

                    if (product != null) {
                        returnBatch.setProduct(product);
                        product.getInventoryBatches().add(returnBatch);
                    } else {
                        returnBatch.setMbp(mbp);
                        mbp.getInventoryBatches().add(returnBatch);
                    }

                    logger.info("Created return batch for size '{}' with {} units (order item {})",
                            orderedSize, quantityToRestore, item.getOrderItemId());
                }

                // Save parent
                if (product != null) {
                    productRepository.save(product);
                } else if (mbp != null) {
                    mbpRepository.save(mbp);
                }
            }
        }

        orderEntity.setOrderStatus("CANCELLED");
        OrderEntity cancelledOrder = orderRepository.save(orderEntity);

        logger.info("Order cancelled successfully with ID: {}", orderId);
        return mapToResponseDto(cancelledOrder);
    }
}