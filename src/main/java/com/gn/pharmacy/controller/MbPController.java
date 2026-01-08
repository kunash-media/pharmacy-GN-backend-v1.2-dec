package com.gn.pharmacy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gn.pharmacy.dto.request.MbPRequestDto;
import com.gn.pharmacy.dto.response.MbPResponseDto;
import com.gn.pharmacy.dto.response.ProductResponseDto;
import com.gn.pharmacy.entity.MbPEntity;
import com.gn.pharmacy.repository.MbPRepository;
import com.gn.pharmacy.service.MbPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mb/products")
public class MbPController {

    private static final Logger logger = LoggerFactory.getLogger(MbPController.class);

    @Autowired
    private MbPService mbpService;

    @Autowired
    private MbPRepository mbpRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/create-product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MbPResponseDto> createMbProduct(
            @RequestPart("productData") String productDataJson,
            @RequestPart("mainImage") MultipartFile mainImage,
            @RequestPart(value = "subImages", required = false) List<MultipartFile> subImages) {

        logger.info("Received request to create MB product");

        try {
            MbPRequestDto dto = objectMapper.readValue(productDataJson, MbPRequestDto.class);

            dto.setMainImage(mainImage);
            dto.setSubImages(subImages != null ? subImages : java.util.Collections.emptyList());

            validateMbProductRequest(dto);

            MbPResponseDto response = mbpService.createMbProduct(dto);
            logger.info("MB Product created successfully with ID: {}, SKU: {}", response.getId(), response.getSku());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for MB product creation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Error creating MB product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MbPResponseDto> getMbProduct(@PathVariable Long id) {
        logger.info("Fetching MB product with ID: {}", id);

        try {
            MbPResponseDto response = mbpService.getMbProductById(id);
            logger.info("MB Product retrieved successfully: {}", response.getTitle());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("MB Product not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving MB product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<MbPResponseDto> getMbProductBySku(@PathVariable String sku) {
        logger.info("Fetching MB product with SKU: {}", sku);

        try {
            String decodedSku = java.net.URLDecoder.decode(sku, java.nio.charset.StandardCharsets.UTF_8);

            MbPResponseDto response = mbpService.getMbProductBySku(decodedSku);
            logger.info("MB Product found with SKU: {}", decodedSku);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("MB Product not found with SKU: {}", sku);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching MB product with SKU '{}': {}", sku, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<MbPResponseDto>> getMbProductsByCategory(@PathVariable String category) {
        logger.info("Fetching MB products by category: {}", category);

        try {
            String decodedCategory = java.net.URLDecoder.decode(category, java.nio.charset.StandardCharsets.UTF_8);

            if (decodedCategory == null || decodedCategory.trim().isEmpty()) {
                logger.warn("MB Product category is null or empty after decoding");
                return ResponseEntity.badRequest().build();
            }

            List<MbPResponseDto> products = mbpService.getMbProductsByCategory(decodedCategory);
            logger.info("Found {} MB products in category '{}'", products.size(), decodedCategory);
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            logger.error("Error processing category parameter: {}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/sub-category/{subCategory}")
    public ResponseEntity<List<MbPResponseDto>> getMbProductsBySubCategory(@PathVariable String subCategory) {
        logger.info("Fetching MB products by sub-category: {}", subCategory);

        try {
            String decodedSubCategory = java.net.URLDecoder.decode(subCategory, java.nio.charset.StandardCharsets.UTF_8);

            List<MbPResponseDto> products = mbpService.getMbProductsBySubCategory(decodedSubCategory);
            logger.info("Found {} MB products in sub-category '{}'", products.size(), decodedSubCategory);
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            logger.error("Error fetching MB products by sub-category '{}': {}", subCategory, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    @GetMapping("/get-all")
//    public ResponseEntity<List<MbPResponseDto>> getAllMbProduct() {
//        logger.info("Fetching all MB products");
//
//        try {
//            List<MbPResponseDto> products = mbpService.getAllMbProduct();
//            logger.info("Retrieved {} MB products", products.size());
//            return ResponseEntity.ok(products);
//
//        } catch (Exception e) {
//            logger.error("Error retrieving MB products: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }


    @GetMapping("/get-all")
    public ResponseEntity<Page<MbPResponseDto>> getAllMbProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        logger.info("Fetching all MB products with pagination - page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, getSort(sort));
            Page<MbPResponseDto> products = mbpService.getAllMbProduct(pageable);

            logger.info("Retrieved {} MB products (total: {})",
                    products.getNumberOfElements(), products.getTotalElements());
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            logger.error("Error retrieving MB products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-all-mb-active-products")
    public ResponseEntity<Page<MbPResponseDto>> getAllActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @RequestParam(defaultValue = "100") long delayMillis) {

        try {
            // Add delay to simulate controlled response rate
            Thread.sleep(delayMillis);

            Pageable pageable = PageRequest.of(page, size, getSort(sort));
            Page<MbPResponseDto> productPage = mbpService.getAllActiveProducts(pageable);

            return new ResponseEntity<>(productPage, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Sort getSort(String[] sort) {
        if (sort.length >= 2) {
            return Sort.by(new Sort.Order(
                    sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC,
                    sort[0]
            ));
        }
        return Sort.unsorted();
    }



//    @GetMapping("/get-all-mb-active-products")
//    public ResponseEntity<List<MbPResponseDto>> getAllProducts(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "100") long delayMillis) {
//        try {
//            // Add delay to simulate controlled response rate
//            Thread.sleep(delayMillis);
//
//            Pageable pageable = PageRequest.of(page, size);
//            Page<MbPResponseDto> productPage = mbpService.getAllProducts(pageable);
//
//            return new ResponseEntity<>(productPage.getContent(), HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MbPResponseDto> updateMbProduct(
            @PathVariable Long id,
            @RequestPart("productData") String productDataJson,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(value = "subImages", required = false) List<MultipartFile> subImages) {

        logger.info("Fully updating MB product with ID: {}", id);

        try {
            MbPRequestDto dto = objectMapper.readValue(productDataJson, MbPRequestDto.class);

            if (mainImage != null && !mainImage.isEmpty()) {
                dto.setMainImage(mainImage);
            }
            if (subImages != null && !subImages.isEmpty()) {
                dto.setSubImages(subImages);
            }

            validateMbProductRequest(dto);

            MbPResponseDto updatedProduct = mbpService.updateMbProduct(id, dto);
            logger.info("MB Product fully updated successfully: {}", updatedProduct.getTitle());

            return ResponseEntity.ok(updatedProduct);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for MB product update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating MB product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping(value = "/update-mb-product/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MbPResponseDto> patchMbProduct(
            @PathVariable Long id,
            @RequestPart(value = "productData", required = false) String productDataJson,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(value = "subImages", required = false) List<MultipartFile> subImages) {

        logger.info("Partially updating MB product with ID: {}", id);

        try {
            MbPRequestDto dto = new MbPRequestDto();

            if (productDataJson != null && !productDataJson.isEmpty()) {
                dto = objectMapper.readValue(productDataJson, MbPRequestDto.class);
            }

            if (mainImage != null && !mainImage.isEmpty()) {
                dto.setMainImage(mainImage);
            }
            if (subImages != null && !subImages.isEmpty()) {
                dto.setSubImages(subImages);
            }

            MbPResponseDto updatedProduct = mbpService.patchMbProduct(id, dto);
            logger.info("MB Product patched successfully: {}", updatedProduct.getTitle());

            return ResponseEntity.ok(updatedProduct);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for MB product patch: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error patching MB product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete-by-id/{id}")
    public ResponseEntity<Void> deleteMbProduct(@PathVariable Long id) {
        logger.info("Deleting MB product with ID: {}", id);

        try {
            mbpService.deleteMbProduct(id);
            logger.info("MB Product deleted successfully with ID: {}", id);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.warn("MB Product not found for deletion with ID: {}", id);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error deleting MB product with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getMbProductMainImage(@PathVariable Long id) {
        logger.info("Fetching main image for MB product ID: {}", id);

        try {
            Optional<MbPEntity> mbp = mbpRepository.findById(id);
            if (mbp.isPresent() && mbp.get().getProductMainImage() != null) {
                logger.debug("MB Product main image found for ID: {}", id);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(mbp.get().getProductMainImage());
            }
            logger.warn("MB Product main image not found for ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching MB product main image for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/subimage/{index}")
    public ResponseEntity<byte[]> getMbProductSubImage(@PathVariable Long id, @PathVariable int index) {
        logger.info("Fetching sub-image {} for MB product ID: {}", index, id);

        try {
            Optional<MbPEntity> mbp = mbpRepository.findById(id);
            if (mbp.isPresent() && index >= 0 && index < mbp.get().getProductSubImages().size()) {
                logger.debug("MB Product sub-image {} found for ID: {}", index, id);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(mbp.get().getProductSubImages().get(index));
            }
            logger.warn("MB Product sub-image {} not found for ID: {}", index, id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching MB product sub-image {} for ID {}: {}", index, id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> checkMbProductExists(@PathVariable Long id) {
        logger.info("Checking if MB product exists with ID: {}", id);

        try {
            boolean exists = mbpRepository.existsById(id);
            logger.debug("MB Product existence check for ID {}: {}", id, exists);
            return ResponseEntity.ok(exists);

        } catch (Exception e) {
            logger.error("Error checking MB product existence for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exists/sku/{sku}")
    public ResponseEntity<Boolean> checkMbProductExistsBySku(@PathVariable String sku) {
        logger.info("Checking if MB product exists with SKU: {}", sku);

        try {
            String decodedSku = java.net.URLDecoder.decode(sku, java.nio.charset.StandardCharsets.UTF_8);

            boolean exists = mbpService.existsBySku(decodedSku);
            logger.debug("MB Product existence check for SKU {}: {}", decodedSku, exists);
            return ResponseEntity.ok(exists);

        } catch (Exception e) {
            logger.error("Error checking MB product existence for SKU {}: {}", sku, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getMbProductCount() {
        logger.info("Getting total MB product count");

        try {
            long count = mbpRepository.count();
            logger.info("Total MB products count: {}", count);
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            logger.error("Error getting MB product count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<MbPResponseDto>> searchMbProducts(@PathVariable String keyword) {
        logger.info("Searching MB products with keyword: {}", keyword);

        try {
            String decodedKeyword = java.net.URLDecoder.decode(keyword, java.nio.charset.StandardCharsets.UTF_8);

            List<MbPResponseDto> result = mbpService.searchMbProducts(decodedKeyword);
            logger.info("Found {} MB products matching keyword: {}", result.size(), decodedKeyword);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error searching MB products by keyword '{}': {}", keyword, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<MbPResponseDto>> getMbProductsByBrand(@PathVariable String brand) {
        logger.info("Fetching MB products by brand: {}", brand);

        try {
            String decodedBrand = java.net.URLDecoder.decode(brand, java.nio.charset.StandardCharsets.UTF_8);

            List<MbPEntity> products = mbpRepository.findByBrand(decodedBrand);
            List<MbPResponseDto> response = products.stream()
                    .map(mbpService::toDto)
                    .collect(java.util.stream.Collectors.toList());

            logger.info("Found {} MB products for brand '{}'", response.size(), decodedBrand);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching MB products by brand '{}': {}", brand, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/in-stock")
    public ResponseEntity<List<MbPResponseDto>> getMbProductsInStock() {
        logger.info("Fetching MB products that are in stock");

        try {
            List<MbPEntity> products = mbpRepository.findByInStockTrue();
            List<MbPResponseDto> response = products.stream()
                    .map(mbpService::toDto)
                    .collect(java.util.stream.Collectors.toList());

            logger.info("Found {} MB products in stock", response.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching MB products in stock: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<MbPResponseDto>> getMbProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {

        logger.info("Fetching MB products in price range {} - {}", minPrice, maxPrice);

        try {
            if (minPrice == null || maxPrice == null || minPrice < 0 || maxPrice < 0 || minPrice > maxPrice) {
                logger.warn("Invalid price range parameters: min={}, max={}", minPrice, maxPrice);
                return ResponseEntity.badRequest().build();
            }

            List<MbPEntity> products = mbpRepository.findByPriceBetween(minPrice, maxPrice);
            List<MbPResponseDto> response = products.stream()
                    .map(mbpService::toDto)
                    .collect(java.util.stream.Collectors.toList());

            logger.info("Found {} MB products in price range {} - {}", response.size(), minPrice, maxPrice);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching MB products by price range: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/discount/{minDiscount}")
    public ResponseEntity<List<MbPResponseDto>> getMbProductsByMinDiscount(@PathVariable Integer minDiscount) {
        logger.info("Fetching MB products with discount >= {}%", minDiscount);

        try {
            if (minDiscount == null || minDiscount < 0 || minDiscount > 100) {
                logger.warn("Invalid discount parameter: {}", minDiscount);
                return ResponseEntity.badRequest().build();
            }

            List<MbPEntity> products = mbpRepository.findByDiscountGreaterThanEqual(minDiscount);
            List<MbPResponseDto> response = products.stream()
                    .map(mbpService::toDto)
                    .collect(java.util.stream.Collectors.toList());

            logger.info("Found {} MB products with discount >= {}%", response.size(), minDiscount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching MB products by discount: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/rating/{minRating}")
    public ResponseEntity<List<MbPResponseDto>> getMbProductsByMinRating(@PathVariable Double minRating) {
        logger.info("Fetching MB products with rating >= {}", minRating);

        try {
            if (minRating == null || minRating < 0 || minRating > 5) {
                logger.warn("Invalid rating parameter: {}", minRating);
                return ResponseEntity.badRequest().build();
            }

            List<MbPEntity> products = mbpRepository.findByRatingGreaterThanEqual(minRating);
            List<MbPResponseDto> response = products.stream()
                    .map(mbpService::toDto)
                    .collect(java.util.stream.Collectors.toList());

            logger.info("Found {} MB products with rating >= {}", response.size(), minRating);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching MB products by rating: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void validateMbProductRequest(MbPRequestDto dto) {
        if (dto.getSku() == null || dto.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (dto.getCategory() == null || dto.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (dto.getPrice() == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (dto.getMainImage() == null || dto.getMainImage().isEmpty()) {
            throw new IllegalArgumentException("Main image is required");
        }
    }
}