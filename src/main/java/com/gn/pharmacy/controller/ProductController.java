package com.gn.pharmacy.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gn.pharmacy.dto.request.ProductRequestDto;
import com.gn.pharmacy.dto.response.BulkUploadResponse;
import com.gn.pharmacy.dto.response.ProductResponseDto;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.service.ProductService;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    private final Logger logger = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService productService, ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @PostMapping("/create-product")
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestPart("productData") String productDataJson,
            @RequestPart("productMainImage") MultipartFile productMainImage,
            @RequestPart(value = "productSubImages", required = false) List<MultipartFile> productSubImages) throws Exception {  // UPDATED: parameter name

        log.info("Request received to create product");

        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequestDto requestDto = objectMapper.readValue(productDataJson, ProductRequestDto.class);

        requestDto.setProductMainImage(productMainImage);  // UPDATED: method name
        requestDto.setProductSubImages(productSubImages);  // UPDATED: method name

        ProductResponseDto responseDto = productService.createProduct(requestDto);
        log.info("Product created successfully with ID: {}", responseDto.getProductId());  // UPDATED: method name
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/get-product/{productId}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable Long productId) {
        log.info("Request received to get product by ID: {}", productId);
        ProductResponseDto responseDto = productService.getProduct(productId);
        log.info("Product retrieved successfully with ID: {}", productId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/get-all-products")
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request received to get all products - page: {}, size: {}", page, size);
        Page<ProductResponseDto> responseDtos = productService.getAllProducts(page, size);
        log.info("Retrieved {} products on page {}", responseDtos.getNumberOfElements(), page);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/get-by-category/{productCategory}")
    public ResponseEntity<List<ProductResponseDto>> getProductsByCategory(@PathVariable String productCategory) {
        log.info("Request received to get products by category: {}", productCategory);

        try {
            // URL decode the category parameter
            String decodedCategory = java.net.URLDecoder.decode(productCategory, java.nio.charset.StandardCharsets.UTF_8);
            log.debug("Decoded category: '{}'", decodedCategory);

            if (decodedCategory == null || decodedCategory.trim().isEmpty()) {
                log.warn("Product category is null or empty after decoding");
                return ResponseEntity.badRequest().build();
            }

            // Debug: Check what the service returns
            List<ProductResponseDto> responseDtos = productService.getProductsByCategory(decodedCategory);
            log.debug("Service returned {} products", responseDtos.size());

            if (responseDtos.isEmpty()) {
                log.warn("No products found for category: '{}'. Checking database...", decodedCategory);
                // Add database check here for debugging
            }

            log.info("Retrieved {} products for category: {}", responseDtos.size(), decodedCategory);
            return ResponseEntity.ok(responseDtos);

        } catch (Exception e) {
            log.error("Error processing category parameter: {}", productCategory, e);
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/get-by-sub-category/{subCategory}")
    public ResponseEntity<List<ProductResponseDto>> getProductsBySubCategory(@PathVariable String subCategory) {
        log.info("Request received to get products by sub category: {}", subCategory);
        List<ProductResponseDto> responseDtos = productService.getProductsBySubCategory(subCategory);
        log.info("Retrieved {} products for sub category: {}", responseDtos.size(), subCategory);
        return ResponseEntity.ok(responseDtos);
    }

    @PutMapping("/update-product/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestPart("productData") String productDataJson,
            @RequestPart("productMainImage") MultipartFile productMainImage,  // UPDATED: parameter name
            @RequestPart(value = "productSubImages", required = false) List<MultipartFile> productSubImages) throws Exception {  // UPDATED: parameter name

        log.info("Request received to update product with ID: {}", id);

        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequestDto requestDto = objectMapper.readValue(productDataJson, ProductRequestDto.class);

        requestDto.setProductMainImage(productMainImage);  // UPDATED: method name
        requestDto.setProductSubImages(productSubImages);  // UPDATED: method name

        ProductResponseDto responseDto = productService.updateProduct(id, requestDto);
        log.info("Product updated successfully with ID: {}", id);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/patch-product/{productId}")
    public ResponseEntity<ProductResponseDto> patchProduct(
            @PathVariable Long productId,
            @RequestPart(value = "productData", required = false) String productDataJson,
            @RequestPart(value = "productMainImage", required = false) MultipartFile productMainImage,  // UPDATED: parameter name
            @RequestPart(value = "productSubImages", required = false) List<MultipartFile> productSubImages) throws Exception {  // UPDATED: parameter name

        log.info("Request received to patch product with ID: {}", productId);

        ProductRequestDto requestDto = new ProductRequestDto();

        if (productDataJson != null && !productDataJson.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            requestDto = objectMapper.readValue(productDataJson, ProductRequestDto.class);
        }

        requestDto.setProductMainImage(productMainImage);  // UPDATED: method name
        requestDto.setProductSubImages(productSubImages);  // UPDATED: method name

        ProductResponseDto responseDto = productService.patchProduct(productId, requestDto);
        log.info("Product patched successfully with ID: {}", productId);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/delete-product/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId) {
        log.info("Request received to delete product with ID: {}", productId);
        productService.deleteProduct(productId);
        log.info("Product deleted successfully with ID: {}", productId);
        return ResponseEntity.status(HttpStatus.OK).body("Product Deleted Successfully!! with ID :" + productId) ;
    }

    @GetMapping("/{productId}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long productId) {
        log.debug("Request received to get main image for product ID: {}", productId);
        Optional<ProductEntity> product = productRepository.findById(productId);
        if (product.isPresent() && product.get().getProductMainImage() != null) {  // UPDATED: method name
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(product.get().getProductMainImage());  // UPDATED: method name
        }
        log.warn("Main image not found for product ID: {}", productId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{productId}/subimage/{index}")
    public ResponseEntity<byte[]> getProductSubImage(@PathVariable Long productId, @PathVariable int index) {
        log.debug("Request received to get sub image {} for product ID: {}", index, productId);
        Optional<ProductEntity> product = productRepository.findById(productId);
        if (product.isPresent() && index < product.get().getProductSubImages().size()) {  // UPDATED: method name
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(product.get().getProductSubImages().get(index));  // UPDATED: method name
        }
        log.warn("Sub image {} not found for product ID: {}", index, productId);
        return ResponseEntity.notFound().build();
    }


    @PostMapping(value = "/bulk-products-upload")
    public ResponseEntity<BulkUploadResponse> bulkUploadProducts(
            @RequestPart(value = "excelFile") MultipartFile excelFile,
            @RequestPart(value = "productImages") List<MultipartFile> images) throws Exception {

        log.info("Request received for bulk product upload");
        BulkUploadResponse response = productService.bulkCreateProducts(excelFile, images);

        log.info("Bulk upload completed: {} products uploaded, {} skipped", response.getUploadedCount(), response.getSkippedCount());
        return ResponseEntity.ok(response);
    }


    //=====================================================================
    //    NEW-PATCH     get-Products-By-Category-Path                        //
    //=====================================================================

    @GetMapping("/get-category-by-path")
    public ResponseEntity<List<ProductResponseDto>> getByCategoryPath(@RequestParam List<String> path) {
        log.info("Fetching products by category path: {}", path);
        List<ProductResponseDto> products = productService.getProductsByCategoryPath(path);
        return ResponseEntity.ok(products);
    }

    //===============================================================
    //    NEW-PATCH     get-Products-By-Sub-Path                        //
    //===============================================================

    @GetMapping("/get-category-by-subpath/{subPath}")
    public ResponseEntity<List<ProductResponseDto>> getBySubPath(@PathVariable String subPath) {
        log.info("Fetching products under subpath: {}", subPath);
        List<ProductResponseDto> products = productService.getProductsBySubPath(subPath);
        return ResponseEntity.ok(products);
    }
}