package com.gn.pharmacy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gn.pharmacy.dto.request.ProductRequestDto;
import com.gn.pharmacy.dto.response.BulkUploadResponse;
import com.gn.pharmacy.dto.response.ProductResponseDto;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public ProductController(ProductService productService, ProductRepository productRepository, ObjectMapper objectMapper) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/create-product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestPart("productData") String productDataJson,
            @RequestPart(value = "productMainImage", required = false) MultipartFile productMainImage,
            @RequestPart(value = "productSubImages", required = false) List<MultipartFile> productSubImages) {

        logger.debug("Received request to create product");

        try {
            ProductRequestDto requestDto = objectMapper.readValue(productDataJson, ProductRequestDto.class);

            // Set images
            if (productMainImage != null && !productMainImage.isEmpty()) {
                requestDto.setProductMainImage(productMainImage);
            }
            if (productSubImages != null && !productSubImages.isEmpty()) {
                requestDto.setProductSubImages(productSubImages);
            }

            // Validate required fields
            validateProductRequest(requestDto);

            ProductResponseDto responseDto = productService.createProduct(requestDto);
            logger.info("Product created successfully with ID: {}", responseDto.getProductId());

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for product creation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long productId) {
        logger.info("Fetching product with ID: {}", productId);

        try {
            ProductResponseDto product = productService.getProduct(productId);
            logger.info("Product retrieved successfully: {}", product.getProductName());

            return ResponseEntity.ok(product);

        } catch (IllegalArgumentException e) {
            logger.warn("Product not found with ID: {}", productId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving product with ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-all-products")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Fetching all products - page: {}, size: {}", page, size);

        try {
            Page<ProductResponseDto> productPage = productService.getAllProducts(page, size);
            logger.info("Retrieved {} products out of {} total",
                    productPage.getNumberOfElements(), productPage.getTotalElements());

            return ResponseEntity.ok(productPage);

        } catch (Exception e) {
            logger.error("Error retrieving products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-by-category/{category}")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(
            @PathVariable String category) {

        logger.info("Fetching products by category: {}", category);

        try {
            // URL decode the category parameter
            String decodedCategory = java.net.URLDecoder.decode(category, java.nio.charset.StandardCharsets.UTF_8);

            if (decodedCategory == null || decodedCategory.trim().isEmpty()) {
                logger.warn("Product category is null or empty after decoding");
                return ResponseEntity.badRequest().build();
            }

            List<ProductResponseDto> products = productService.getProductsByCategory(decodedCategory);
            logger.info("Found {} products in category '{}'", products.size(), decodedCategory);

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            logger.error("Error processing category parameter: {}", category, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get-by-sub-category/{subCategory}")
    public ResponseEntity<List<ProductResponseDto>> getProductsBySubCategory(
            @PathVariable String subCategory) {

        logger.info("Fetching products by sub-category: {}", subCategory);

        try {
            List<ProductResponseDto> products = productService.getProductsBySubCategory(subCategory);
            logger.info("Found {} products in sub-category '{}'", products.size(), subCategory);

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            logger.error("Error fetching products by sub-category '{}': {}", subCategory, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(value = "/update-product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long productId,
            @RequestPart("productData") String productDataJson,
            @RequestPart(value = "productMainImage", required = false) MultipartFile productMainImage,
            @RequestPart(value = "productSubImages", required = false) List<MultipartFile> productSubImages) {

        logger.info("Fully updating product with ID: {}", productId);

        try {
            ProductRequestDto requestDto = objectMapper.readValue(productDataJson, ProductRequestDto.class);

            // Set images
            if (productMainImage != null && !productMainImage.isEmpty()) {
                requestDto.setProductMainImage(productMainImage);
            }
            if (productSubImages != null && !productSubImages.isEmpty()) {
                requestDto.setProductSubImages(productSubImages);
            }

            // Validate required fields
            validateProductRequest(requestDto);

            ProductResponseDto updatedProduct = productService.updateProduct(productId, requestDto);
            logger.info("Product fully updated successfully: {}", updatedProduct.getProductName());

            return ResponseEntity.ok(updatedProduct);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for product update: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error updating product with ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping(value = "/patch-product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponseDto> patchProduct(
            @PathVariable Long productId,
            @RequestPart(value = "productData", required = false) String productDataJson,
            @RequestPart(value = "productMainImage", required = false) MultipartFile productMainImage,
            @RequestPart(value = "productSubImages", required = false) List<MultipartFile> productSubImages) {

        logger.info("Partially updating product with ID: {}", productId);

        try {
            ProductRequestDto requestDto = new ProductRequestDto();

            if (productDataJson != null && !productDataJson.isEmpty()) {
                requestDto = objectMapper.readValue(productDataJson, ProductRequestDto.class);
            }

            // Set images if provided
            if (productMainImage != null && !productMainImage.isEmpty()) {
                requestDto.setProductMainImage(productMainImage);
            }
            if (productSubImages != null && !productSubImages.isEmpty()) {
                requestDto.setProductSubImages(productSubImages);
            }

            ProductResponseDto updatedProduct = productService.patchProduct(productId, requestDto);
            logger.info("Product patched successfully: {}", updatedProduct.getProductName());

            return ResponseEntity.ok(updatedProduct);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for product patch: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error patching product with ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete-product/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        logger.info("Deleting product with ID: {}", productId);

        try {
            productService.deleteProduct(productId);
            logger.info("Product deleted successfully with ID: {}", productId);

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Product not found for deletion with ID: {}", productId);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error deleting product with ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{productId}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId) {
        logger.info("Fetching image for product ID: {}", productId);

        try {
            Optional<ProductEntity> product = productRepository.findById(productId);
            if (product.isPresent() && product.get().getProductMainImage() != null) {
                logger.debug("Product image found for ID: {}", productId);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(product.get().getProductMainImage());
            }
            logger.warn("Product image not found for ID: {}", productId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching product image for ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{productId}/subimage/{index}")
    public ResponseEntity<byte[]> getProductSubImage(@PathVariable Long productId, @PathVariable int index) {
        logger.info("Fetching sub-image {} for product ID: {}", index, productId);

        try {
            Optional<ProductEntity> product = productRepository.findById(productId);
            if (product.isPresent() && index >= 0 && index < product.get().getProductSubImages().size()) {
                logger.debug("Product sub-image {} found for ID: {}", index, productId);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(product.get().getProductSubImages().get(index));
            }
            logger.warn("Product sub-image {} not found for ID: {}", index, productId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching product sub-image {} for ID {}: {}", index, productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkUploadResponse> bulkUploadProducts(
            @RequestPart("excelFile") MultipartFile excelFile,
            @RequestPart(value = "productImages", required = false) List<MultipartFile> images) {

        logger.info("Request received for bulk product upload");

        try {
            BulkUploadResponse response = productService.bulkCreateProducts(excelFile, images);
            logger.info("Bulk upload completed: {} uploaded, {} skipped",
                    response.getUploadedCount(), response.getSkippedCount());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid input for bulk upload: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error during bulk upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-category-by-path")
    public ResponseEntity<List<ProductResponseDto>> getByCategoryPath(
            @RequestParam List<String> path) {

        logger.info("Fetching products by category path: {}", path);

        try {
            List<ProductResponseDto> products = productService.getProductsByCategoryPath(path);
            logger.info("Found {} products for category path: {}", products.size(), path);

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            logger.error("Error fetching products by category path: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-category-by-subpath/{subPath}")
    public ResponseEntity<List<ProductResponseDto>> getBySubPath(@PathVariable String subPath) {
        logger.info("Fetching products under subpath: {}", subPath);

        try {
            List<ProductResponseDto> products = productService.getProductsBySubPath(subPath);
            logger.info("Found {} products under subpath: {}", products.size(), subPath);

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            logger.error("Error fetching products by subpath '{}': {}", subPath, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exists/{productId}")
    public ResponseEntity<Boolean> checkProductExists(@PathVariable Long productId) {
        logger.info("Checking if product exists with ID: {}", productId);

        try {
            boolean exists = productRepository.existsById(productId);
            logger.debug("Product existence check for ID {}: {}", productId, exists);

            return ResponseEntity.ok(exists);

        } catch (Exception e) {
            logger.error("Error checking product existence for ID {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getProductCount() {
        logger.info("Getting total product count");

        try {
            long count = productRepository.count();
            logger.info("Total products count: {}", count);

            return ResponseEntity.ok(count);

        } catch (Exception e) {
            logger.error("Error getting product count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/{productName}")
    public ResponseEntity<List<ProductResponseDto>> searchProductsByName(@PathVariable String productName) {
        logger.info("Searching products with name containing: {}", productName);

        try {
            List<ProductEntity> products = productRepository.findAll();
            List<ProductResponseDto> result = products.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(productName.toLowerCase()))
                    .map(this::mapToResponseDto)
                    .collect(java.util.stream.Collectors.toList());

            logger.info("Found {} products matching name: {}", result.size(), productName);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error searching products by name '{}': {}", productName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-by-sku/{sku}")
    public ResponseEntity<ProductResponseDto> getProductBySku(@PathVariable String sku) {
        logger.info("Fetching product with SKU: {}", sku);

        try {
            List<ProductEntity> products = productRepository.findAll();
            Optional<ProductEntity> product = products.stream()
                    .filter(p -> sku.equals(p.getSku()))
                    .findFirst();

            if (product.isPresent()) {
                logger.info("Product found with SKU: {}", sku);
                return ResponseEntity.ok(mapToResponseDto(product.get()));
            } else {
                logger.warn("Product not found with SKU: {}", sku);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            logger.error("Error fetching product with SKU '{}': {}", sku, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void validateProductRequest(ProductRequestDto requestDto) {
        if (requestDto.getProductName() == null || requestDto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (requestDto.getProductPrice() == null) {
            throw new IllegalArgumentException("Product price is required");
        }
        if (requestDto.getProductQuantity() == null) {
            throw new IllegalArgumentException("Product quantity is required");
        }
        if (requestDto.getProductMainImage() == null || requestDto.getProductMainImage().isEmpty()) {
            throw new IllegalArgumentException("Product main image is required");
        }
    }

    private ProductResponseDto mapToResponseDto(ProductEntity entity) {
        ProductResponseDto responseDto = new ProductResponseDto();

        responseDto.setProductId(entity.getProductId());
        responseDto.setSku(entity.getSku());
        responseDto.setProductName(entity.getProductName());
        responseDto.setProductCategory(entity.getProductCategory());
        responseDto.setProductSubCategory(entity.getProductSubCategory());
        responseDto.setProductPrice(entity.getProductPrice());
        responseDto.setProductOldPrice(entity.getProductOldPrice());
        responseDto.setProductStock(entity.getProductStock());
        responseDto.setProductStatus(entity.getProductStatus());
        responseDto.setProductDescription(entity.getProductDescription());
        responseDto.setCreatedAt(entity.getCreatedAt());
        responseDto.setProductQuantity(entity.getProductQuantity());
        responseDto.setPrescriptionRequired(entity.isPrescriptionRequired());
        responseDto.setBrandName(entity.getBrandName());
        responseDto.setMfgDate(entity.getMfgDate());
        responseDto.setExpDate(entity.getExpDate());
        responseDto.setBatchNo(entity.getBatchNo());
        responseDto.setRating(entity.getRating());
        responseDto.setBenefitsList(entity.getBenefitsList() != null ? entity.getBenefitsList() : new java.util.ArrayList<>());
        responseDto.setIngredientsList(entity.getIngredientsList() != null ? entity.getIngredientsList() : new java.util.ArrayList<>());
        responseDto.setDirectionsList(entity.getDirectionsList() != null ? entity.getDirectionsList() : new java.util.ArrayList<>());
        responseDto.setCategoryPath(entity.getCategoryPath() != null ? entity.getCategoryPath() : new java.util.ArrayList<>());

        // Set image URLs
        if (entity.getProductMainImage() != null) {
            responseDto.setProductMainImage("/api/products/" + entity.getProductId() + "/image");
        }

        if (entity.getProductSubImages() != null && !entity.getProductSubImages().isEmpty()) {
            List<String> subImageUrls = java.util.stream.IntStream.range(0, entity.getProductSubImages().size())
                    .mapToObj(i -> "/api/products/" + entity.getProductId() + "/subimage/" + i)
                    .collect(java.util.stream.Collectors.toList());
            responseDto.setProductSubImages(subImageUrls);
        }

        responseDto.setProductDynamicFields(entity.getProductDynamicFields());
        responseDto.setProductSizes(entity.getProductSizes() != null ? entity.getProductSizes() : new java.util.ArrayList<>());

        return responseDto;
    }
}