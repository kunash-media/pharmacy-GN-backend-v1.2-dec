package com.gn.pharmacy.controller;

import com.gn.pharmacy.entity.MbPEntity;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.entity.UserEntity;
import com.gn.pharmacy.entity.WishlistItemEntity;
import com.gn.pharmacy.repository.MbPRepository;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.repository.UserRepository;
import com.gn.pharmacy.repository.WishlistItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);

    @Autowired private WishlistItemRepository wishlistItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository; // Added for fetching UserEntity
    @Autowired private MbPRepository mbPRepository;

    @PostMapping("/add-wishlist-items")
    @Transactional
    public ResponseEntity<Map<String, Object>> addToWishlist(@RequestBody Map<String, Object> request) {
        logger.info("Add to wishlist request: {}", request);
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long productId = Long.parseLong(request.get("productId").toString());

            String productTypeStr = (String) request.get("productType");
            if (productTypeStr == null || productTypeStr.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "productType is required"));
            }
            WishlistItemEntity.ProductType productType;
            try {
                productType = WishlistItemEntity.ProductType.valueOf(productTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid productType. Must be MEDICINE, MOTHER, or BABY"));
            }

            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            UserEntity user = userOpt.get();

            WishlistItemEntity item = new WishlistItemEntity();
            item.setUser(user);
            item.setProductType(productType);

            Optional<WishlistItemEntity> existing = Optional.empty();

            if (productType == WishlistItemEntity.ProductType.MEDICINE) {
                Optional<ProductEntity> productOpt = productRepository.findById(productId);
                if (productOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Medicine product not found"));
                }
                item.setProduct(productOpt.get());
                item.setMbP(null);
                existing = wishlistItemRepository.findByUserAndProduct(user, productOpt.get());
            } else {
                // MOTHER or BABY
                Optional<MbPEntity> mbProductOpt = mbPRepository.findById(productId);
                if (mbProductOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Mother/Baby product not found"));
                }
                item.setMbP(mbProductOpt.get());
                item.setProduct(null);
                existing = wishlistItemRepository.findByUserAndMbP(user, mbProductOpt.get());
            }

            if (existing.isPresent()) {
                WishlistItemEntity existingItem = existing.get();
                existingItem.setProductType(productType);
                wishlistItemRepository.save(existingItem);
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Already in wishlist",
                        "wishlistItemId", existingItem.getId()
                ));
            }

            wishlistItemRepository.save(item);

            logger.info("Added to wishlist: userId={}, productId={}, productType={}", userId, productId, productType);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Added to wishlist",
                    "wishlistItemId", item.getId()
            ));

        } catch (Exception e) {
            logger.error("Error adding to wishlist", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request: " + e.getMessage()));
        }
    }

    @GetMapping("/get-wishlist-items")
    public ResponseEntity<List<Map<String, Object>>> getWishlist(@RequestParam Long userId) {
        logger.info("Fetching wishlist for userId: {}", userId);
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<WishlistItemEntity> items = wishlistItemRepository.findByUserIdWithProduct(userId);
        List<Map<String, Object>> response = new ArrayList<>();

        for (WishlistItemEntity item : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("wishlistItemId", item.getId());
            map.put("productType", item.getProductType() != null ? item.getProductType().name() : "MEDICINE");
            map.put("addedDate", item.getAddedDate());

            WishlistItemEntity.ProductType type = item.getProductType();

            if (type == WishlistItemEntity.ProductType.MOTHER || type == WishlistItemEntity.ProductType.BABY) {
                MbPEntity mbProduct = item.getMbP();
                if (mbProduct != null) {
                    map.put("productId", mbProduct.getId());
                    map.put("title", mbProduct.getTitle());
                    map.put("price", mbProduct.getPrice());
                    map.put("originalPrice", mbProduct.getOriginalPrice());
                    map.put("imageUrl", "/api/mb/products/" + mbProduct.getId() + "/image");
                    map.put("subImageUrls", getMbSubImageUrls(mbProduct.getId(),
                            mbProduct.getProductSubImages() != null ? mbProduct.getProductSubImages().size() : 0));
                } else {
                    map.put("productId", 0L);
                    map.put("title", "Product Not Found (Mother/Baby)");
                    map.put("price", 0.0);
                    map.put("originalPrice", 0.0);
                    map.put("imageUrl", "");
                    map.put("subImageUrls", List.of());
                }
            } else {
                // MEDICINE
                ProductEntity product = item.getProduct();
                if (product != null) {
                    map.put("productId", product.getProductId());
                    map.put("title", product.getProductName());
                    map.put("price", product.getProductPrice());
                    map.put("originalPrice", product.getProductPrice()); // Change if you have separate MRP
                    map.put("imageUrl", "/api/products/" + product.getProductId() + "/image");
                    map.put("subImageUrls", getSubImageUrls(product.getProductId(),
                            product.getProductSubImages() != null ? product.getProductSubImages().size() : 0));
                } else {
                    map.put("productId", 0L);
                    map.put("title", "Product Not Found (Medicine)");
                    map.put("price", 0.0);
                    map.put("originalPrice", 0.0);
                    map.put("imageUrl", "");
                    map.put("subImageUrls", List.of());
                }
            }

            response.add(map);
        }

        logger.info("Retrieved {} wishlist items for userId: {}", response.size(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/remove-wishlist-items")
    @Transactional
    public ResponseEntity<String> removeFromWishlist(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long productId = Long.parseLong(request.get("productId").toString());
            String productTypeStr = (String) request.get("productType");

            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

            WishlistItemEntity.ProductType productType = WishlistItemEntity.ProductType.MEDICINE;
            if (productTypeStr != null) {
                try {
                    productType = WishlistItemEntity.ProductType.valueOf(productTypeStr.toUpperCase());
                } catch (Exception e) {
                    // default to MEDICINE
                }
            }

            Optional<WishlistItemEntity> itemOpt = Optional.empty();

            if (productType == WishlistItemEntity.ProductType.MEDICINE) {
                Optional<ProductEntity> productOpt = productRepository.findById(productId);
                if (productOpt.isEmpty()) return ResponseEntity.badRequest().body("Medicine not found");
                itemOpt = wishlistItemRepository.findByUserAndProduct(userOpt.get(), productOpt.get());
            } else {
                Optional<MbPEntity> mbOpt = mbPRepository.findById(productId);
                if (mbOpt.isEmpty()) return ResponseEntity.badRequest().body("Product not found");
                itemOpt = wishlistItemRepository.findByUserAndMbP(userOpt.get(), mbOpt.get());
            }

            if (itemOpt.isPresent()) {
                wishlistItemRepository.delete(itemOpt.get());
                return ResponseEntity.ok("Removed");
            }
            return ResponseEntity.badRequest().body("Item not in wishlist");
        } catch (Exception e) {
            logger.error("Remove error", e);
            return ResponseEntity.badRequest().body("Invalid request");
        }
    }

    @PostMapping("/merge-wishlist-items")
    @Transactional
    public ResponseEntity<String> mergeWishlist(@RequestBody Map<String, Object> request) {
        logger.info("Merge wishlist request: {}", request);
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            List<Map<String, Object>> localWishlist = (List<Map<String, Object>>) request.get("items");

            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");
            UserEntity user = userOpt.get();

            if (localWishlist == null || localWishlist.isEmpty()) {
                return ResponseEntity.ok("No items to merge");
            }

            int addedCount = 0;
            for (Map<String, Object> localItem : localWishlist) {
                Long productId = Long.parseLong(localItem.get("id").toString());
                String productTypeStr = (String) localItem.get("productType");

                WishlistItemEntity.ProductType productType = WishlistItemEntity.ProductType.MEDICINE;
                if (productTypeStr != null && !productTypeStr.isEmpty()) {
                    try {
                        productType = WishlistItemEntity.ProductType.valueOf(productTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warn("Invalid productType in merge: {}, using MEDICINE", productTypeStr);
                    }
                }

                Optional<ProductEntity> productOpt = productRepository.findById(productId);
                if (productOpt.isEmpty()) continue;

                ProductEntity product = productOpt.get();
                Optional<WishlistItemEntity> existing = wishlistItemRepository.findByUserAndProduct(user, product);

                if (!existing.isPresent()) {
                    WishlistItemEntity item = new WishlistItemEntity();
                    item.setUser(user);
                    item.setProduct(product);
                    item.setProductType(productType);
                    wishlistItemRepository.save(item);
                    addedCount++;
                } else {
                    WishlistItemEntity item = existing.get();
                    if (item.getProductType() != productType) {
                        item.setProductType(productType);
                        wishlistItemRepository.save(item);
                    }
                }
            }

            return ResponseEntity.ok("Wishlist merged successfully. Added: " + addedCount);
        } catch (Exception e) {
            logger.error("Error merging wishlist", e);
            return ResponseEntity.badRequest().body("Merge failed");
        }
    }

    @PostMapping("/clear-wishlist")
    @Transactional
    public ResponseEntity<String> clearWishlist(@RequestBody Map<String, Object> request) {
        logger.info("Clear wishlist request");
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Optional<UserEntity> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

            wishlistItemRepository.deleteByUser(userOpt.get());
            return ResponseEntity.ok("Wishlist cleared successfully");
        } catch (Exception e) {
            logger.error("Error clearing wishlist", e);
            return ResponseEntity.badRequest().body("Clear failed");
        }
    }



    // Helper methods for subimages
    private List<String> getSubImageUrls(Long productId, int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> "/api/products/" + productId + "/subimage/" + i)
                .toList();
    }

    private List<String> getMbSubImageUrls(Long productId, int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> "/api/mb/products/" + productId + "/subimage/" + i)
                .toList();
    }
}