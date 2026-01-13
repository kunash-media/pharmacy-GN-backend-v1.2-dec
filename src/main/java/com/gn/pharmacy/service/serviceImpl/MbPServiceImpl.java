package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.request.MbPRequestDto;
import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.MbPResponseDto;
import com.gn.pharmacy.entity.InventoryEntity;
import com.gn.pharmacy.entity.MbPEntity;
import com.gn.pharmacy.repository.InventoryRepository;
import com.gn.pharmacy.repository.MbPRepository;
import com.gn.pharmacy.service.InventoryService;
import com.gn.pharmacy.service.MbPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MbPServiceImpl implements MbPService {

    private static final Logger logger = LoggerFactory.getLogger(MbPServiceImpl.class);

    @Autowired private InventoryService inventoryService;

    @Autowired
    private MbPRepository repo;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public MbPResponseDto createMbProduct(MbPRequestDto dto) {
        logger.info("Creating new MB product with SKU: {}", dto.getSku());

        try {
            if (repo.existsBySku(dto.getSku())) {
                logger.error("SKU already exists: {}", dto.getSku());
                throw new IllegalArgumentException("SKU already exists: " + dto.getSku());
            }

            MbPEntity entity = toEntity(dto, new MbPEntity());
            entity = repo.save(entity);

            BatchInfoDTO batchInfo = new BatchInfoDTO();
            batchInfo.setMbpId(entity.getId());
            batchInfo.setBatchNo("MB-DEFAULT-" + entity.getId());  // Optional/default if not in DTO
            batchInfo.setQuantity(dto.getStockQuantity());
            batchInfo.setMfgDate(null);  // Optional, add to DTO if needed
            batchInfo.setExpiryDate(null);  // Optional, add to DTO if needed
            inventoryService.addStockBatch(batchInfo);

            MbPResponseDto response = toDto(entity);
            logger.info("MB Product created successfully with ID: {}, SKU: {}", response.getId(), response.getSku());

            return response;

        } catch (Exception e) {
            logger.error("Error creating MB product with SKU {}: {}", dto.getSku(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public MbPResponseDto updateMbProduct(Long id, MbPRequestDto dto) {
        logger.info("Fully updating MB product with ID: {}", id);

        try {
            MbPEntity entity = repo.findById(id).orElseThrow(() -> {
                logger.warn("MB Product not found with ID: {}", id);
                return new IllegalArgumentException("MB Product not found with ID: " + id);
            });

            if (!entity.getSku().equals(dto.getSku())) {
                if (repo.existsBySku(dto.getSku())) {
                    logger.error("SKU already exists: {}", dto.getSku());
                    throw new IllegalArgumentException("SKU already exists: " + dto.getSku());
                }
            }

            entity = toEntity(dto, entity);
            entity = repo.save(entity);

            MbPResponseDto response = toDto(entity);
            logger.info("MB Product fully updated successfully with ID: {}, SKU: {}", response.getId(), response.getSku());

            return response;

        } catch (Exception e) {
            logger.error("Error updating MB product with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public MbPResponseDto patchMbProduct(Long id, MbPRequestDto dto) {
        logger.info("Partially updating MB product with ID: {}", id);

        try {
            MbPEntity entity = repo.findById(id).orElseThrow(() -> {
                logger.warn("MB Product not found with ID: {}", id);
                return new IllegalArgumentException("MB Product not found with ID: " + id);
            });

            if (StringUtils.hasText(dto.getSku()) && !dto.getSku().equals(entity.getSku())) {
                if (repo.existsBySku(dto.getSku())) {
                    logger.error("SKU already exists: {}", dto.getSku());
                    throw new IllegalArgumentException("SKU already exists: " + dto.getSku());
                }
            }

            patchEntity(dto, entity);
            entity = repo.save(entity);

            MbPResponseDto response = toDto(entity);
            logger.info("MB Product patched successfully with ID: {}, SKU: {}", response.getId(), response.getSku());

            return response;

        } catch (Exception e) {
            logger.error("Error patching MB product with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public MbPResponseDto getMbProductById(Long id) {
        logger.debug("Fetching MB product by ID: {}", id);

        try {
            MbPEntity entity = repo.findById(id).orElseThrow(() -> {
                logger.warn("MB Product not found with ID: {}", id);
                return new IllegalArgumentException("MB Product not found with ID: " + id);
            });

            MbPResponseDto response = toDto(entity);
            logger.debug("MB Product fetched successfully with ID: {}, Title: {}", response.getId(), response.getTitle());

            return response;

        } catch (Exception e) {
            logger.error("Error fetching MB product with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public MbPResponseDto getMbProductBySku(String sku) {
        logger.info("Fetching MB product by SKU: {}", sku);

        try {
            MbPEntity entity = repo.findBySku(sku).orElseThrow(() -> {
                logger.warn("MB Product not found with SKU: {}", sku);
                return new IllegalArgumentException("MB Product not found with SKU: " + sku);
            });

            MbPResponseDto response = toDto(entity);
            logger.info("MB Product fetched successfully with SKU: {}, Title: {}", sku, response.getTitle());

            return response;

        } catch (Exception e) {
            logger.error("Error fetching MB product with SKU {}: {}", sku, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<MbPResponseDto> getAllMbProduct() {
        return List.of();
    }


//    @Override
//    public List<MbPResponseDto> getAllMbProduct() {
//        logger.debug("Fetching all MB products");
//
//        try {
//            List<MbPResponseDto> products = repo.findAll().stream()
//                    .map(this::toDto)
//                    .collect(Collectors.toList());
//
//            logger.debug("Fetched {} MB products", products.size());
//            return products;
//
//        } catch (Exception e) {
//            logger.error("Error fetching all MB products: {}", e.getMessage(), e);
//            throw e;
//        }
//    }

    @Override
    @Transactional(readOnly = true)
    public Page<MbPResponseDto> getAllMbProduct(Pageable pageable) {
        logger.debug("Fetching all MB products with pagination");

        try {
            Page<MbPEntity> entities = repo.findAllWithPagination(pageable);
            logger.debug("Fetched {} MB products (page {})",
                    entities.getNumberOfElements(), pageable.getPageNumber());
            return entities.map(this::toDto);

        } catch (Exception e) {
            logger.error("Error fetching all MB products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve all products: " + e.getMessage(), e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public Page<MbPResponseDto> getAllActiveProducts(Pageable pageable) {
        try {
            Page<MbPEntity> entities = repo.findAllActive(pageable);
            return entities.map(this::toDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve active products: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<MbPResponseDto> getAllActiveProducts() {
        try {
            List<MbPEntity> entities = repo.findAllActive();
            return entities.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve active products: " + e.getMessage(), e);
        }
    }

    //============= NEW DELETD GET ALL HANDLED ====================//

    @Transactional(readOnly = true)
    @Override
    public Page<MbPResponseDto> getAllProducts(Pageable pageable) {
        try {
            Page<MbPEntity> entities = repo.findAllActive(pageable);
            return entities.map(this::toDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve products: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<MbPResponseDto> getAllProducts() {
        try {
            List<MbPEntity> entities = repo.findAllActive();
            // Use stream to map each entity using mapToResponseDto
            return entities.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve products: " + e.getMessage(), e);
        }
    }

    //========================== END =========================================//


    @Override
    public List<MbPResponseDto> getMbProductsByCategory(String category) {
        logger.info("Fetching MB products by category: {}", category);

        try {
            List<MbPResponseDto> products = repo.findByProductCategoryAndNotDeleted(category).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} MB products in category '{}'", products.size(), category);
            return products;

        } catch (Exception e) {
            logger.error("Error fetching MB products by category '{}': {}", category, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<MbPResponseDto> getMbProductsBySubCategory(String subCategory) {
        logger.info("Fetching MB products by sub-category: {}", subCategory);

        try {
            List<MbPResponseDto> products = repo.findBySubCategory(subCategory).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} MB products in sub-category '{}'", products.size(), subCategory);
            return products;

        } catch (Exception e) {
            logger.error("Error fetching MB products by sub-category '{}': {}", subCategory, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<MbPResponseDto> searchMbProducts(String keyword) {
        logger.info("Searching MB products with keyword: {}", keyword);

        try {
            List<MbPEntity> titleResults = repo.findByTitleContainingIgnoreCase(keyword);
            List<MbPEntity> skuResults = repo.findBySkuContainingIgnoreCase(keyword);
            List<MbPEntity> brandResults = repo.findByBrandContainingIgnoreCase(keyword);

            Set<MbPEntity> combinedResults = new HashSet<>();
            combinedResults.addAll(titleResults);
            combinedResults.addAll(skuResults);
            combinedResults.addAll(brandResults);

            List<MbPResponseDto> result = combinedResults.stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());

            logger.info("Found {} MB products matching keyword: {}", result.size(), keyword);
            return result;

        } catch (Exception e) {
            logger.error("Error searching MB products by keyword '{}': {}", keyword, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean existsBySku(String sku) {
        logger.debug("Checking if MB product exists with SKU: {}", sku);

        try {
            boolean exists = repo.existsBySku(sku);
            logger.debug("MB Product existence check for SKU {}: {}", sku, exists);
            return exists;

        } catch (Exception e) {
            logger.error("Error checking MB product existence for SKU {}: {}", sku, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteMbProduct(Long id) {
        logger.info("Deleting MB product with ID: {}", id);

        try {
            if (!repo.existsById(id)) {
                logger.warn("MB Product not found for deletion with ID: {}", id);
                throw new IllegalArgumentException("MB Product not found with ID: " + id);
            }

            repo.deleteById(id);
            logger.info("MB Product deleted successfully with ID: {}", id);

        } catch (Exception e) {
            logger.error("Error deleting MB product with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    private MbPEntity toEntity(MbPRequestDto d, MbPEntity e) {
        logger.debug("Converting DTO to Entity for MB product");

        e.setSku(d.getSku());
        e.setTitle(d.getTitle());
        e.setCategory(d.getCategory());
        e.setSubCategory(d.getSubCategory());
        e.setPrice(d.getPrice());
        e.setOriginalPrice(d.getOriginalPrice());
        e.setDiscount(d.getDiscount());
        e.setRating(d.getRating());
        e.setReviewCount(d.getReviewCount());
        e.setBrand(d.getBrand());
        e.setInStock(d.getInStock());
        e.setStockQuantity(d.getStockQuantity());
        e.setDescription(d.getDescription());
        e.setProductSizes(d.getProductSizes());
        e.setFeatures(d.getFeatures());
        e.setSpecifications(d.getSpecifications());

        try {
            if (d.getMainImage() != null && !d.getMainImage().isEmpty()) {
                e.setProductMainImage(d.getMainImage().getBytes());
                logger.debug("Main image set for MB product");
            }
            if (d.getSubImages() != null && !d.getSubImages().isEmpty()) {
                e.getProductSubImages().clear();
                for (var f : d.getSubImages()) {
                    if (f != null && !f.isEmpty()) {
                        e.getProductSubImages().add(f.getBytes());
                    }
                }
                logger.debug("{} sub-images set for MB product", d.getSubImages().size());
            }

            // NEW: Set approved field if provided in request (null = keep existing/default)
            if (d.getApproved() != null) {
                e.setApproved(d.getApproved());
            }
        } catch (Exception ex) {
            logger.error("Error processing images for MB product: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error processing images", ex);
        }
        return e;
    }



    private void patchEntity(MbPRequestDto d, MbPEntity e) {
        logger.debug("Patching MB entity with partial data");

        if (StringUtils.hasText(d.getSku())) e.setSku(d.getSku());
        if (StringUtils.hasText(d.getTitle())) e.setTitle(d.getTitle());
        if (StringUtils.hasText(d.getCategory())) e.setCategory(d.getCategory());
        if (StringUtils.hasText(d.getSubCategory())) e.setSubCategory(d.getSubCategory());

        // FIX: Only update price if DTO has non-empty list (not null and not empty)
        if (d.getPrice() != null && !d.getPrice().isEmpty()) {
            e.setPrice(d.getPrice());
            logger.debug("Price updated to: {}", d.getPrice());
        } else if (d.getPrice() != null && d.getPrice().isEmpty()) {
            logger.warn("Empty price list provided - preserving existing price: {}", e.getPrice());
            // Don't update - keep existing price
        }

        // FIX: Only update originalPrice if DTO has non-empty list
        if (d.getOriginalPrice() != null && !d.getOriginalPrice().isEmpty()) {
            e.setOriginalPrice(d.getOriginalPrice());
            logger.debug("OriginalPrice updated to: {}", d.getOriginalPrice());
        } else if (d.getOriginalPrice() != null && d.getOriginalPrice().isEmpty()) {
            logger.warn("Empty originalPrice list provided - preserving existing: {}", e.getOriginalPrice());
            // Don't update - keep existing originalPrice
        }

//        if (d.getPrice() != null) e.setPrice(d.getPrice());
//        if (d.getOriginalPrice() != null) e.setOriginalPrice(d.getOriginalPrice());


        if (d.getDiscount() != null) e.setDiscount(d.getDiscount());
        if (d.getRating() != null) e.setRating(d.getRating());
        if (d.getReviewCount() != null) e.setReviewCount(d.getReviewCount());
        if (StringUtils.hasText(d.getBrand())) e.setBrand(d.getBrand());
        if (d.getInStock() != null) e.setInStock(d.getInStock());
        if (d.getStockQuantity() != null) e.setStockQuantity(d.getStockQuantity());
        if (d.getDescription() != null && !d.getDescription().isEmpty()) e.setDescription(d.getDescription());
        if (d.getProductSizes() != null && !d.getProductSizes().isEmpty()) e.setProductSizes(d.getProductSizes());
        if (d.getFeatures() != null && !d.getFeatures().isEmpty()) e.setFeatures(d.getFeatures());
        if (StringUtils.hasText(d.getSpecifications())) e.setSpecifications(d.getSpecifications());

        // NEW: Patch approved field if provided
        if (d.getApproved() != null) {
            e.setApproved(d.getApproved());
        }
        if (d.getMainImage() != null && !d.getMainImage().isEmpty()) {
            try {
                e.setProductMainImage(d.getMainImage().getBytes());
                logger.debug("Main image updated for MB product");
            } catch (Exception ex) {
                logger.error("Error updating main image: {}", ex.getMessage());
            }
        }

        if (d.getSubImages() != null && !d.getSubImages().isEmpty()) {
            try {
                e.getProductSubImages().clear();
                for (var f : d.getSubImages()) {
                    if (f != null && !f.isEmpty()) {
                        e.getProductSubImages().add(f.getBytes());
                    }
                }
                logger.debug("Sub-images updated for MB product");
            } catch (Exception ex) {
                logger.error("Error updating sub-images: {}", ex.getMessage());
            }
        }
    }

//    @Override
//    public MbPResponseDto toDto(MbPEntity e) {
//        logger.debug("Converting Entity to DTO for MB product ID: {}", e.getId());
//
//        MbPResponseDto d = new MbPResponseDto();
//        d.setId(e.getId());
//        d.setSku(e.getSku());
//        d.setTitle(e.getTitle());
//        d.setCategory(e.getCategory());
//        d.setSubCategory(e.getSubCategory());
//        d.setPrice(e.getPrice());
//        d.setOriginalPrice(e.getOriginalPrice());
//        d.setDiscount(e.getDiscount());
//        d.setRating(e.getRating());
//        d.setReviewCount(e.getReviewCount());
//        d.setBrand(e.getBrand());
//        d.setInStock(e.getInStock());
//        d.setStockQuantity(e.getStockQuantity());
//        d.setDescription(e.getDescription());
//        d.setProductSizes(e.getProductSizes());
//        d.setFeatures(e.getFeatures());
//        d.setSpecifications(e.getSpecifications());
//        d.setCreatedAt(e.getCreatedAt());
//
//        Long id = e.getId();
//        d.setMainImageUrl("/api/mb/products/" + id + "/image");
//
//        List<String> subUrls = IntStream.range(0, e.getProductSubImages().size())
//                .mapToObj(i -> "/api/mb/products/" + id + "/subimage/" + i)
//                .collect(Collectors.toList());
//        d.setSubImageUrls(subUrls);
//
//        logger.debug("DTO conversion completed for MB product ID: {}", e.getId());
//
//        return d;
//    }


    @Override
    public MbPResponseDto toDto(MbPEntity e) {
        logger.debug("Converting Entity to DTO for MB product ID: {}", e.getId());

        MbPResponseDto d = new MbPResponseDto();
        d.setId(e.getId());
        d.setSku(e.getSku());
        d.setTitle(e.getTitle());
        d.setCategory(e.getCategory());
        d.setSubCategory(e.getSubCategory());
        d.setPrice(e.getPrice());
        d.setOriginalPrice(e.getOriginalPrice());
        d.setDiscount(e.getDiscount());
        d.setRating(e.getRating());
        d.setReviewCount(e.getReviewCount());
        d.setBrand(e.getBrand());
        d.setInStock(e.getInStock());  // This can stay as-is (boolean based on your business rule, e.g., total > 0)
        d.setDescription(e.getDescription());
        d.setProductSizes(e.getProductSizes());
        d.setFeatures(e.getFeatures());
        d.setSpecifications(e.getSpecifications());
        d.setCreatedAt(e.getCreatedAt());

        d.setApproved(e.isApproved());
        d.setDeleted(e.isDeleted());

        Long id = e.getId();
        d.setMainImageUrl("/api/mb/products/" + id + "/image");

        List<String> subUrls = IntStream.range(0, e.getProductSubImages().size())
                .mapToObj(i -> "/api/mb/products/" + id + "/subimage/" + i)
                .collect(Collectors.toList());
        d.setSubImageUrls(subUrls);

        // ==================== INVENTORY MAPPING BLOCK ====================
        List<InventoryEntity> inventories = inventoryRepository.findByMbp(e);

        if (!inventories.isEmpty()) {
            // Total stock quantity across all batches
            int totalQuantity = inventories.stream()
                    .mapToInt(InventoryEntity::getQuantity)
                    .sum();
            d.setStockQuantity(totalQuantity);

            // Optional: Get latest batch details (uncomment if your MbPResponseDto has these fields)
            Optional<InventoryEntity> latestOpt = inventoryRepository
                    .findFirstByMbpOrderByLastUpdatedDesc(e);

            latestOpt.ifPresent(latest -> {
                // Uncomment these lines only if you have added these fields to MbPResponseDto
                // d.setBatchNo(latest.getBatchNo());
                // d.setMfgDate(latest.getMfgDate());
                // d.setExpDate(latest.getExpDate());
            });
        } else {
            // No inventory yet
            d.setStockQuantity(0);
            // If you have batch fields in DTO, set them to null here
            // d.setBatchNo(null);
            // d.setMfgDate(null);
            // d.setExpDate(null);
        }
        // ================================================================

        logger.debug("DTO conversion completed for MB product ID: {}", e.getId());
        return d;
    }
}