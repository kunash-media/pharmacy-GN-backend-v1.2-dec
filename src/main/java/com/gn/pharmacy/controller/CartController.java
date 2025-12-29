package com.gn.pharmacy.controller;

import com.gn.pharmacy.entity.CartItemEntity;
import com.gn.pharmacy.entity.MbPEntity;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.entity.UserEntity;
import com.gn.pharmacy.repository.CartItemRepository;
import com.gn.pharmacy.repository.MbPRepository;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private MbPRepository mbpRepository;
    @Autowired private UserRepository userRepository; // Added

    @PostMapping("/add-cart-items")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Object> request) {
        logger.info("Add to cart request: {}", request);
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String type = (String) request.get("type"); // "PRODUCT" or "MBP"
            Integer quantity = Integer.parseInt(request.getOrDefault("quantity", "1").toString());
            String size = request.getOrDefault("selectedSize", "").toString();

            // New: Get productType from request
            String productTypeStr = (String) request.get("productType");
            if (productTypeStr == null || productTypeStr.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "productType is required"));
            }

            CartItemEntity.ProductType productType;
            try {
                productType = CartItemEntity.ProductType.valueOf(productTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid productType. Must be MEDICINE, MOTHER, or BABY"));
            }

            if (userId == null || type == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));
            }
            if (quantity < 1) {
                return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be positive"));
            }

            // Get user entity
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            UserEntity user = userOpt.get();

            CartItemEntity item;
            Optional<CartItemEntity> existing;

            if ("PRODUCT".equalsIgnoreCase(type)) {
                // Get productId from request
                if (!request.containsKey("productId")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "productId is required for PRODUCT type"));
                }
                Long productId = Long.parseLong(request.get("productId").toString());

                Optional<ProductEntity> productOpt = productRepository.findById(productId);
                if (productOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Product not found"));
                }
                ProductEntity product = productOpt.get();

                logger.debug("Found product: {}, sizes available: {}", product.getProductName(), product.getProductSizes());

                // Check size only if product has sizes defined and size is provided
                if (product.getProductSizes() != null && !product.getProductSizes().isEmpty() &&
                        !product.getProductSizes().contains(size) && !size.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid size for product. Available sizes: " + product.getProductSizes()));
                }

                existing = cartItemRepository.findByUserAndProductAndSelectedSize(user, product, size);
                if (existing.isPresent()) {
                    item = existing.get();
                    item.setQuantity(item.getQuantity() + quantity); // Add to existing quantity
                    item.setProductType(productType); // Update product type
                    logger.debug("Updating existing cart item for product: {}", productId);
                } else {
                    item = new CartItemEntity();
                    item.setUser(user);
                    item.setProduct(product);
                    item.setProductType(productType);
                    item.setQuantity(quantity);
                    item.setSelectedSize(size);
                    logger.debug("Creating new cart item for product: {}", productId);
                }
            } else if ("MBP".equalsIgnoreCase(type)) {
                // Get mbpId from request
                if (!request.containsKey("mbpId")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "mbpId is required for MBP type"));
                }
                Long mbpId = Long.parseLong(request.get("mbpId").toString());

                Optional<MbPEntity> mbpOpt = mbpRepository.findById(mbpId);
                if (mbpOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "MBP not found"));
                }
                MbPEntity mbp = mbpOpt.get();

                logger.debug("Found MBP: {}, sizes available: {}", mbp.getTitle(), mbp.getSizes());

                // Check size only if MBP has sizes defined and size is provided
                if (mbp.getSizes() != null && !mbp.getSizes().isEmpty() &&
                        !mbp.getSizes().contains(size) && !size.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid size for MBP. Available sizes: " + mbp.getSizes()));
                }

                existing = cartItemRepository.findByUserAndMbpAndSelectedSize(user, mbp, size);
                if (existing.isPresent()) {
                    item = existing.get();
                    item.setQuantity(item.getQuantity() + quantity); // Add to existing quantity
                    item.setProductType(productType); // Update product type
                    logger.debug("Updating existing cart item for MBP: {}", mbpId);
                } else {
                    item = new CartItemEntity();
                    item.setUser(user);
                    item.setMbp(mbp);
                    item.setProductType(productType);
                    item.setQuantity(quantity);
                    item.setSelectedSize(size);
                    logger.debug("Creating new cart item for MBP: {}", mbpId);
                }
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid type. Must be PRODUCT or MBP"));
            }

            cartItemRepository.save(item);
            logger.info("Cart item saved successfully. CartItemId: {}", item.getId());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Added to cart",
                    "cartItemId", item.getId(),
                    "quantity", item.getQuantity(),
                    "productType", productType.name()
            ));
        } catch (Exception e) {
            logger.error("Error adding to cart", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request data: " + e.getMessage()));
        }
    }

    @GetMapping("/get-cart-items")
    public ResponseEntity<List<Map<String, Object>>> getCart(@RequestParam Long userId) {
        if (userId == null) return ResponseEntity.badRequest().build();

        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        UserEntity user = userOpt.get();
        List<CartItemEntity> items = cartItemRepository.findByUserWithDetails(user);
        List<Map<String, Object>> response = new ArrayList<>();

        for (CartItemEntity item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("cartItemId", item.getId());
            map.put("quantity", item.getQuantity());
            map.put("selectedSize", item.getSelectedSize());
            map.put("addedDate", item.getAddedDate());
            map.put("productType", item.getProductType().name()); // Include product type

            if (item.getProduct() != null) {
                ProductEntity p = item.getProduct();
                map.put("type", "PRODUCT");
                map.put("itemId", p.getProductId());
                map.put("title", p.getProductName());
                map.put("price", p.getProductPrice());
                map.put("imageUrl", "/api/products/" + p.getProductId() + "/image");
                map.put("productSizes", p.getProductSizes());
                map.put("subImageUrls", getSubImageUrls(p.getProductId(), p.getProductSubImages() != null ? p.getProductSubImages().size() : 0));
            } else if (item.getMbp() != null) {
                MbPEntity m = item.getMbp();
                map.put("type", "MBP");
                map.put("itemId", m.getId());
                map.put("title", m.getTitle());
                map.put("price", m.getPrice());
                map.put("imageUrl", "/api/mb/products/" + m.getId() + "/image");
                map.put("subImageUrls", getSubImageUrls(m.getId(), m.getProductSubImages() != null ? m.getProductSubImages().size() : 0));
            }
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove-cart-items")
    public ResponseEntity<String> removeFromCart(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String type = (String) request.get("type");
            String size = request.getOrDefault("selectedSize", "").toString();

            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            UserEntity user = userOpt.get();

            Optional<CartItemEntity> existing = Optional.empty();
            if ("PRODUCT".equalsIgnoreCase(type)) {
                if (!request.containsKey("productId")) {
                    return ResponseEntity.badRequest().body("productId is required for PRODUCT type");
                }
                Long productId = Long.parseLong(request.get("productId").toString());
                existing = cartItemRepository.findByUserAndProductIdAndSize(user, productId, size);
            } else if ("MBP".equalsIgnoreCase(type)) {
                if (!request.containsKey("mbpId")) {
                    return ResponseEntity.badRequest().body("mbpId is required for MBP type");
                }
                Long mbpId = Long.parseLong(request.get("mbpId").toString());
                existing = cartItemRepository.findByUserAndMbpIdAndSize(user, mbpId, size);
            } else {
                return ResponseEntity.badRequest().body("Invalid type");
            }

            if (existing.isPresent()) {
                cartItemRepository.delete(existing.get());
                return ResponseEntity.ok("Removed from cart");
            }
            return ResponseEntity.badRequest().body("Item not found");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request");
        }
    }

    @PostMapping("/update-cart-items")
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestBody Map<String, Object> request) {
        logger.info("Update cart item request: {}", request);
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String type = (String) request.get("type");
            Integer quantity = Integer.parseInt(request.get("quantity").toString());
            String size = request.getOrDefault("selectedSize", "").toString();

            // New: Update product type if provided
            CartItemEntity.ProductType productType = null;
            if (request.containsKey("productType")) {
                String productTypeStr = (String) request.get("productType");
                try {
                    productType = CartItemEntity.ProductType.valueOf(productTypeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid productType"));
                }
            }

            if (quantity <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be positive"));
            }

            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            UserEntity user = userOpt.get();

            Optional<CartItemEntity> existing = Optional.empty();

            if ("PRODUCT".equalsIgnoreCase(type)) {
                if (!request.containsKey("productId")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "productId is required for PRODUCT type"));
                }
                Long productId = Long.parseLong(request.get("productId").toString());

                // First, find the product to verify it exists
                Optional<ProductEntity> productOpt = productRepository.findById(productId);
                if (productOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Product with ID " + productId + " not found in database"));
                }

                existing = cartItemRepository.findByUserAndProductIdAndSize(user, productId, size);
                if (existing.isEmpty()) {
                    logger.warn("Cart item not found - User: {}, ProductId: {}, Size: {}", userId, productId, size);
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Cart item not found",
                            "details", "No cart item found for user " + userId + ", product " + productId + ", size '" + size + "'"
                    ));
                }

            } else if ("MBP".equalsIgnoreCase(type)) {
                if (!request.containsKey("mbpId")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "mbpId is required for MBP type"));
                }
                Long mbpId = Long.parseLong(request.get("mbpId").toString());

                // First, find the MBP to verify it exists
                Optional<MbPEntity> mbpOpt = mbpRepository.findById(mbpId);
                if (mbpOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "MBP with ID " + mbpId + " not found in database"));
                }

                existing = cartItemRepository.findByUserAndMbpIdAndSize(user, mbpId, size);
                if (existing.isEmpty()) {
                    logger.warn("Cart item not found - User: {}, MbpId: {}, Size: {}", userId, mbpId, size);
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Cart item not found",
                            "details", "No cart item found for user " + userId + ", MBP " + mbpId + ", size '" + size + "'"
                    ));
                }

            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid type. Must be PRODUCT or MBP"));
            }

            if (existing.isPresent()) {
                CartItemEntity item = existing.get();
                item.setQuantity(quantity);
                if (productType != null) {
                    item.setProductType(productType);
                }
                cartItemRepository.save(item);
                logger.info("Cart item updated successfully - CartItemId: {}, New Quantity: {}", item.getId(), quantity);

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Cart updated successfully",
                        "cartItemId", item.getId(),
                        "newQuantity", quantity
                ));
            }

            return ResponseEntity.badRequest().body(Map.of("error", "Item not found in cart"));

        } catch (Exception e) {
            logger.error("Error updating cart item", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    @PostMapping("/merge-cart-items")
    public ResponseEntity<String> mergeCart(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            List<Map<String, Object>> localItems = (List<Map<String, Object>>) request.get("items");

            if (localItems == null || localItems.isEmpty()) {
                return ResponseEntity.ok("No items to merge");
            }

            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            UserEntity user = userOpt.get();

            for (Map<String, Object> local : localItems) {
                String type = (String) local.get("type");
                Integer qty = Integer.parseInt(local.getOrDefault("quantity", "1").toString());
                String size = local.getOrDefault("selectedSize", "").toString();
                String productTypeStr = (String) local.get("productType");

                CartItemEntity.ProductType productType = CartItemEntity.ProductType.MEDICINE; // default
                if (productTypeStr != null && !productTypeStr.isEmpty()) {
                    try {
                        productType = CartItemEntity.ProductType.valueOf(productTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid productType during merge, using default: {}", productTypeStr);
                    }
                }

                CartItemEntity item = null;
                if ("PRODUCT".equalsIgnoreCase(type)) {
                    if (!local.containsKey("productId")) {
                        continue; // Skip if productId not provided
                    }
                    Long productId = Long.parseLong(local.get("productId").toString());
                    Optional<ProductEntity> p = productRepository.findById(productId);
                    if (p.isPresent()) {
                        // Check size only if product has sizes
                        if (p.get().getProductSizes() != null && !p.get().getProductSizes().contains(size)) {
                            continue; // Skip invalid size
                        }
                        Optional<CartItemEntity> existing = cartItemRepository.findByUserAndProductAndSelectedSize(user, p.get(), size);
                        if (existing.isPresent()) {
                            item = existing.get();
                            item.setQuantity(qty);
                            item.setProductType(productType);
                        } else {
                            item = new CartItemEntity();
                            item.setUser(user);
                            item.setProduct(p.get());
                            item.setProductType(productType);
                            item.setQuantity(qty);
                            item.setSelectedSize(size);
                        }
                    }
                } else if ("MBP".equalsIgnoreCase(type)) {
                    if (!local.containsKey("mbpId")) {
                        continue; // Skip if mbpId not provided
                    }
                    Long mbpId = Long.parseLong(local.get("mbpId").toString());
                    Optional<MbPEntity> m = mbpRepository.findById(mbpId);
                    if (m.isPresent()) {
                        // Check size only if MBP has sizes
                        if (m.get().getSizes() != null && !m.get().getSizes().contains(size)) {
                            continue; // Skip invalid size
                        }
                        Optional<CartItemEntity> existing = cartItemRepository.findByUserAndMbpAndSelectedSize(user, m.get(), size);
                        if (existing.isPresent()) {
                            item = existing.get();
                            item.setQuantity(qty);
                            item.setProductType(productType);
                        } else {
                            item = new CartItemEntity();
                            item.setUser(user);
                            item.setMbp(m.get());
                            item.setProductType(productType);
                            item.setQuantity(qty);
                            item.setSelectedSize(size);
                        }
                    }
                }
                if (item != null) {
                    cartItemRepository.save(item);
                }
            }
            return ResponseEntity.ok("Cart merged");
        } catch (Exception e) {
            logger.error("Merge error", e);
            return ResponseEntity.badRequest().body("Merge failed");
        }
    }

    @PostMapping("/clear-cart")
    @Transactional
    public ResponseEntity<String> clearCart(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
            UserEntity user = userOpt.get();
            cartItemRepository.deleteByUser(user);
            return ResponseEntity.ok("Cart cleared");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Clear failed");
        }
    }

    private List<String> getSubImageUrls(Long id, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> "/api/products/" + id + "/subimage/" + i)
                .collect(Collectors.toList());
    }
}