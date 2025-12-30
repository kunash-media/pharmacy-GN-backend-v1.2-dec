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
        this.mbpRepository =  mbpRepository;
    }



    @Override
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {

        logger.info("Creating new order for userId: {}", orderRequestDto.getUserId());

        // Validate user
        UserEntity user = userRepository.findById(orderRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + orderRequestDto.getUserId()));

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUser(user);
        orderEntity.setOrderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")));
        mapOrderFields(orderRequestDto, orderEntity);

        // Save order first to get orderId (needed for order items)
        OrderEntity savedEntity = orderRepository.save(orderEntity);

        List<OrderItemEntity> orderItemsToSave = new ArrayList<>();

        // Process inventory deduction and validate order items
        // Process inventory deduction and validate order items
        if (orderRequestDto.getOrderItems() != null && !orderRequestDto.getOrderItems().isEmpty()) {

            for (OrderItemDto itemDto : orderRequestDto.getOrderItems()) {

                if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                    throw new RuntimeException("Invalid quantity for item: " + itemDto.getItemName());
                }

                Long productId = itemDto.getProductId();
                Long mbpId = itemDto.getMbpId();

                // CRITICAL VALIDATION: Exactly one of productId or mbpId must be present
                if ((productId == null && mbpId == null) || (productId != null && mbpId != null)) {
                    throw new RuntimeException(
                            "Invalid order item: exactly one of 'productId' or 'mbpId' must be provided. " +
                                    "Received productId=" + productId + ", mbpId=" + mbpId +
                                    ", itemName=" + itemDto.getItemName()
                    );
                }

                List<InventoryEntity> batches = null;
                Object parentEntity = null;
                Long itemId = null;
                String itemType = "";

                if (productId != null) {
                    ProductEntity product = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
                    batches = product.getInventoryBatches();
                    parentEntity = product;
                    itemId = productId;
                    itemType = "Product";
                } else { // mbpId != null
                    MbPEntity mbp = mbpRepository.findById(mbpId)
                            .orElseThrow(() -> new RuntimeException("MbP product not found with ID: " + mbpId));
                    batches = mbp.getInventoryBatches();
                    parentEntity = mbp;
                    itemId = mbpId;
                    itemType = "MbP";
                }

                if (batches == null || batches.isEmpty()) {
                    throw new RuntimeException("No inventory batches found for " + itemType + " ID: " + itemId);
                }

                int required = itemDto.getQuantity();
                int remainingToDeduct = required;

                // Sort by inventoryId (FIFO - oldest first)
                batches.sort(Comparator.comparing(InventoryEntity::getInventoryId));

                for (InventoryEntity batch : batches) {
                    if (remainingToDeduct <= 0) break;

                    if (batch.getQuantity() > 0) {
                        int deduct = Math.min(batch.getQuantity(), remainingToDeduct);
                        batch.setQuantity(batch.getQuantity() - deduct);
                        remainingToDeduct -= deduct;

                        logger.info("Deducted {} from batch {} ({} ID: {}). Remaining: {}",
                                deduct, batch.getBatchNo(), itemType, itemId, batch.getQuantity());
                    }
                }

                if (remainingToDeduct > 0) {
                    int available = batches.stream().mapToInt(InventoryEntity::getQuantity).sum();
                    throw new RuntimeException(
                            "Insufficient stock for " + itemType + " ID: " + itemId +
                                    ". Required: " + required + ", Available: " + available
                    );
                }

                // Save updated inventory (Product or MbP)
                if (parentEntity instanceof ProductEntity) {
                    productRepository.save((ProductEntity) parentEntity);
                } else if (parentEntity instanceof MbPEntity) {
                    mbpRepository.save((MbPEntity) parentEntity);
                }

                // Create and ADD OrderItemEntity to the collection (cascade will handle saving)
                OrderItemEntity orderItem = createOrderItemEntity(itemDto, savedEntity, parentEntity);
                savedEntity.getOrderItems().add(orderItem);
            }

            // Save the order entity (cascades to items, populates IDs in collection)
            savedEntity = orderRepository.save(savedEntity);
        } else {
            logger.warn("Order created with no items for userId: {}", orderRequestDto.getUserId());
        }

        logger.info("Order created successfully with ID: {}", savedEntity.getOrderId());
        return mapToResponseDto(savedEntity);
    }


//    @Override
//    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
//
//        logger.info("Creating new order");
//
//        OrderEntity orderEntity = new OrderEntity();
//
//        UserEntity user = userRepository.findById(orderRequestDto.getUserId())
//                .orElseThrow(() -> new RuntimeException("User not found with ID: " + orderRequestDto.getUserId()));
//        orderEntity.setUser(user);
//
//        orderEntity.setOrderDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a")));
//        mapOrderFields(orderRequestDto, orderEntity);
//
//        OrderEntity savedEntity = orderRepository.save(orderEntity);
//
//
//        // NEW FIX : Deduct product quantities
//        // Deduct from actual inventory batches (FIFO: oldest batches first)
//
//        if (orderRequestDto.getOrderItems() != null && !orderRequestDto.getOrderItems().isEmpty()) {
//            for (OrderItemDto itemDto : orderRequestDto.getOrderItems()) {
//                if (itemDto.getQuantity() <= 0) {
//                    continue;
//                }
//
//                List<InventoryEntity> batches = null;
//                Object entityToSave = null;
//
//                if (itemDto.getProductId() != null) {
//                    // Existing flow for ProductEntity
//                    ProductEntity product = productRepository.findById(itemDto.getProductId())
//                            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDto.getProductId()));
//                    batches = product.getInventoryBatches();
//                    entityToSave = product;
//                } else if (itemDto.getMbpId() != null) {
//                    // New flow for MbPEntity (mirroring ProductEntity)
//                    MbPEntity mbp = mbpRepository.findById(itemDto.getMbpId())
//                            .orElseThrow(() -> new RuntimeException("MbP not found with ID: " + itemDto.getMbpId()));
//                    batches = mbp.getInventoryBatches();
//                    entityToSave = mbp;
//                } else {
//                    continue;  // Skip if neither ID is provided
//                }
//
//                if (batches == null || batches.isEmpty()) {
//                    throw new RuntimeException("No inventory available for item with ID: " +
//                            (itemDto.getProductId() != null ? itemDto.getProductId() : itemDto.getMbpId()));
//                }
//
//                int required = itemDto.getQuantity();
//                int remainingToDeduct = required;
//
//                // Sort batches by oldest first (FIFO by ID; adjust comparator if needed, e.g., by mfgDate)
//                batches.sort(Comparator.comparing(InventoryEntity::getInventoryId));
//
//                for (InventoryEntity batch : batches) {
//                    if (remainingToDeduct <= 0) break;
//
//                    if (batch.getQuantity() > 0) {
//                        int deductFromThisBatch = Math.min(batch.getQuantity(), remainingToDeduct);
//                        batch.setQuantity(batch.getQuantity() - deductFromThisBatch);
//                        remainingToDeduct -= deductFromThisBatch;
//
//                        logger.info("Deducted {} from batch {} (item ID: {}). Remaining in batch: {}",
//                                deductFromThisBatch, batch.getBatchNo(),
//                                (itemDto.getProductId() != null ? itemDto.getProductId() : itemDto.getMbpId()),
//                                batch.getQuantity());
//                    }
//                }
//
//                if (remainingToDeduct > 0) {
//                    throw new RuntimeException("Insufficient inventory for item ID: " +
//                            (itemDto.getProductId() != null ? itemDto.getProductId() : itemDto.getMbpId()) +
//                            ". Required: " + required + ", Available across batches: " +
//                            batches.stream().mapToInt(InventoryEntity::getQuantity).sum());
//                }
//
//                // Save the updated entity (ProductEntity or MbPEntity)
//                if (entityToSave instanceof ProductEntity) {
//                    productRepository.save((ProductEntity) entityToSave);
//                } else if (entityToSave instanceof MbPEntity) {
//                    mbpRepository.save((MbPEntity) entityToSave);
//                }
//            }
//
//            // Existing code to create and save order items (unchanged)
//            List<OrderItemEntity> orderItems = orderRequestDto.getOrderItems().stream()
//                    .map(itemDto -> createOrderItemEntity(itemDto, savedEntity))
//                    .collect(Collectors.toList());
//            orderItemRepository.saveAll(orderItems);
//            savedEntity.setOrderItems(orderItems);
//        }
//
//
//        if (orderRequestDto.getOrderItems() != null && !orderRequestDto.getOrderItems().isEmpty()) {
//            for (OrderItemDto itemDto : orderRequestDto.getOrderItems()) {
//                if (itemDto.getProductId() == null || itemDto.getQuantity() <= 0) {
//                    continue;
//                }
//
//                // Fetch product with inventory batches
//                ProductEntity product = productRepository.findById(itemDto.getProductId())
//                        .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDto.getProductId()));
//
//                // Load inventory batches (assuming lazy, but since we're in transaction, it's ok)
//                List<InventoryEntity> batches = product.getInventoryBatches();
//                if (batches == null || batches.isEmpty()) {
//                    throw new RuntimeException("No inventory available for product ID: " + itemDto.getProductId());
//                }
//
//                int required = itemDto.getQuantity();
//                int remainingToDeduct = required;
//
//                // Sort batches by oldest first (you can change criteria: mfgDate, inventoryId, etc.)
//                batches.sort(Comparator.comparing(InventoryEntity::getInventoryId)); // FIFO by ID (or parse mfgDate if needed)
//
//                for (InventoryEntity batch : batches) {
//                    if (remainingToDeduct <= 0) break;
//
//                    if (batch.getQuantity() > 0) {
//                        int deductFromThisBatch = Math.min(batch.getQuantity(), remainingToDeduct);
//                        batch.setQuantity(batch.getQuantity() - deductFromThisBatch);
//                        remainingToDeduct -= deductFromThisBatch;
//
//                        logger.info("Deducted {} from batch {} (product ID: {}). Remaining in batch: {}",
//                                deductFromThisBatch, batch.getBatchNo(), product.getProductId(), batch.getQuantity());
//                    }
//                }
//
//                if (remainingToDeduct > 0) {
//                    throw new RuntimeException("Insufficient inventory for product ID: " + itemDto.getProductId() +
//                            ". Required: " + required + ", Available across batches: " +
//                            batches.stream().mapToInt(InventoryEntity::getQuantity).sum());
//                }
//
//                // Save updated batches (cascade should handle it, but safe to save product)
//                productRepository.save(product);
//            }
//
//            // After deduction, proceed to create order items...
//            List<OrderItemEntity> orderItems = orderRequestDto.getOrderItems().stream()
//                    .map(itemDto -> createOrderItemEntity(itemDto, savedEntity))
//                    .collect(Collectors.toList());
//            orderItemRepository.saveAll(orderItems);
//            savedEntity.setOrderItems(orderItems);
//        }
//
//        logger.info("Order created with ID: {}", savedEntity.getOrderId());
//        return mapToResponseDto(savedEntity);
//    }


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

//    private OrderItemEntity createOrderItemEntity(OrderItemDto itemDto, OrderEntity orderEntity) {
//        OrderItemEntity orderItemEntity = new OrderItemEntity();
//        orderItemEntity.setOrder(orderEntity);
//
//        // FIX: Use the values from the DTO instead of fetching from database
//        // This preserves the prices at the time of order placement
//        if (itemDto.getProductId() != null) {
//            ProductEntity product = productRepository.findById(itemDto.getProductId())
//                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDto.getProductId()));
//            orderItemEntity.setProduct(product);
//        }
//
//        // Always use the values from the request DTO
//        orderItemEntity.setItemName(itemDto.getItemName());
//        orderItemEntity.setItemPrice(itemDto.getItemPrice());
//        orderItemEntity.setItemOldPrice(itemDto.getItemOldPrice());
//        orderItemEntity.setQuantity(itemDto.getQuantity());
//        orderItemEntity.setSubtotal(itemDto.getSubtotal());
//
//        return orderItemEntity;
//    }


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

                        // === MAIN IMAGE URL LOGIC ===
                        String mainImageUrl = null;

                        if (item.getProduct() != null && item.getProduct().getProductId() != null) {
                            // Regular Product - uses /api/products/{id}/image
                            mainImageUrl = "/api/products/" + item.getProduct().getProductId() + "/image";

                        } else if (item.getMbP() != null && item.getMbP().getId() != null) {
                            // MbP Product - uses /api/mb/products/{id}/image
                            mainImageUrl = "/api/mb/products/" + item.getMbP().getId() + "/image";
                        }

                        itemDto.setProductMainImage(mainImageUrl);

                        return itemDto;
                    })
                    .collect(Collectors.toList());

            responseDto.setOrderItems(orderItemDtos);
        } else {
            responseDto.setOrderItems(new ArrayList<>()); // Ensure never null
        }

        return responseDto;
    }


//    private OrderResponseDto mapToResponseDto(OrderEntity orderEntity) {
//        OrderResponseDto responseDto = new OrderResponseDto();
//        responseDto.setOrderId(orderEntity.getOrderId());
//        responseDto.setUserId(orderEntity.getUser() != null ? orderEntity.getUser().getUserId() : null);
//        responseDto.setShippingAddress(orderEntity.getShippingAddress());
//        responseDto.setShippingAddress2(orderEntity.getShippingAddress2());
//        responseDto.setShippingCity(orderEntity.getShippingCity());
//        responseDto.setShippingState(orderEntity.getShippingState());
//        responseDto.setShippingPincode(orderEntity.getShippingPincode());
//        responseDto.setShippingCountry(orderEntity.getShippingCountry());
//        responseDto.setShippingFirstName(orderEntity.getShippingFirstName());
//        responseDto.setShippingLastName(orderEntity.getShippingLastName());
//        responseDto.setShippingEmail(orderEntity.getShippingEmail());
//        responseDto.setShippingPhone(orderEntity.getShippingPhone());
//        responseDto.setCustomerFirstName(orderEntity.getCustomerFirstName());
//        responseDto.setCustomerLastName(orderEntity.getCustomerLastName());
//        responseDto.setCustomerPhone(orderEntity.getCustomerPhone());
//        responseDto.setCustomerEmail(orderEntity.getCustomerEmail());
//        responseDto.setPaymentMethod(orderEntity.getPaymentMethod());
//        responseDto.setTotalAmount(orderEntity.getTotalAmount());
//        responseDto.setTax(orderEntity.getTax());
//        responseDto.setCouponApplied(orderEntity.getCouponApplied());
//        responseDto.setConvenienceFee(orderEntity.getConvenienceFee());
//        responseDto.setDiscountPercent(orderEntity.getDiscountPercent());
//        responseDto.setDiscountAmount(orderEntity.getDiscountAmount());
//        responseDto.setOrderStatus(orderEntity.getOrderStatus());
//        responseDto.setOrderDate(orderEntity.getOrderDate());
//        responseDto.setDeliveryDate(orderEntity.getDeliveryDate());
//
//        if (orderEntity.getOrderItems() != null) {
//            List<OrderItemDto> orderItemDtos = orderEntity.getOrderItems().stream()
//                    .map(item -> {
//                        OrderItemDto itemDto = new OrderItemDto();
//                        itemDto.setOrderItemId(item.getOrderItemId());
//                        itemDto.setProductId(item.getProduct() != null ? item.getProduct().getProductId() : null);
//                        itemDto.setQuantity(item.getQuantity());
//                        itemDto.setItemPrice(item.getItemPrice());
//                        itemDto.setItemOldPrice(item.getItemOldPrice());
//                        itemDto.setSubtotal(item.getSubtotal());
//                        itemDto.setItemName(item.getItemName());
//                        // ADD MAIN IMAGE URL
//                        if (item.getProduct() != null && item.getProduct().getProductId() != null) {
//                            String imageUrl = "/api/products/" + item.getProduct().getProductId() + "/image";
//                            itemDto.setProductMainImage(imageUrl);
//                        }
//                        return itemDto;
//                    })
//                    .collect(Collectors.toList());
//            responseDto.setOrderItems(orderItemDtos);
//        }
//
//        return responseDto;
//    }


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

        // Restore inventory on cancellation
        if (orderEntity.getOrderItems() != null && !orderEntity.getOrderItems().isEmpty()) {
            for (OrderItemEntity item : orderEntity.getOrderItems()) {
                ProductEntity product = item.getProduct();
                if (product == null) continue;

                int quantityToRestore = item.getQuantity();
                if (quantityToRestore <= 0) continue;

                List<InventoryEntity> batches = product.getInventoryBatches();
                if (batches == null || batches.isEmpty()) {
                    // Optional: create a new "returned" batch? Or just skip?
                    // For now, create a new batch for returned stock
                    InventoryEntity returnedBatch = new InventoryEntity();
                    returnedBatch.setProduct(product);
                    returnedBatch.setBatchNo("RETURNED-" + orderId + "-" + item.getOrderItemId());
                    returnedBatch.setMfgDate("N/A");
                    returnedBatch.setExpDate("N/A");
                    returnedBatch.setQuantity(quantityToRestore);
                    returnedBatch.setStockStatus("AVAILABLE");
                    batches.add(returnedBatch);
                    product.getInventoryBatches().add(returnedBatch);
                    logger.info("Created returned batch for {} units of product ID: {}", quantityToRestore, product.getProductId());
                } else {
                    // Add back to newest batch (last in list) - or you can sort reverse
                    batches.sort(Comparator.comparing(InventoryEntity::getInventoryId).reversed()); // newest first

                    int remainingToRestore = quantityToRestore;
                    for (InventoryEntity batch : batches) {
                        if (remainingToRestore <= 0) break;
                        int addToThisBatch = remainingToRestore;
                        batch.setQuantity(batch.getQuantity() + addToThisBatch);
                        remainingToRestore -= addToThisBatch;
                        logger.info("Restored {} units to batch {} (product ID: {})", addToThisBatch, batch.getBatchNo(), product.getProductId());
                    }
                }

                productRepository.save(product);
            }
        }

//        if (orderEntity.getOrderItems() != null) {
//
//            for (OrderItemEntity item : orderEntity.getOrderItems()) {
//                if (item.getProduct() != null) {
//
//                    ProductEntity product = item.getProduct();
//                    Integer currentStockQuantity = product.getProductQuantity();
//                    Integer restoredStock = currentStockQuantity + item.getQuantity();
//                    product.setProductQuantity(restoredStock);
//
//                    productRepository.save(product);
//                    logger.info("Restored {} units to product ID: {}. New stock: {}",
//                            item.getQuantity(), product.getProductId(), restoredStock);
//                }
//            }
//        }

        // Update order status to CANCELLED
        orderEntity.setOrderStatus("CANCELLED");
        OrderEntity cancelledOrder = orderRepository.save(orderEntity);

        logger.info("Order cancelled successfully with ID: {}", orderId);
        return mapToResponseDto(cancelledOrder);
    }




}