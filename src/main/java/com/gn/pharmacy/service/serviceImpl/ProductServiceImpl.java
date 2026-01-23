package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.request.ProductPatchDto;
import com.gn.pharmacy.dto.request.ProductRequestDto;
import com.gn.pharmacy.dto.response.BulkUploadResponse;
import com.gn.pharmacy.dto.response.ProductResponseDto;
import com.gn.pharmacy.entity.BatchVariant;
import com.gn.pharmacy.entity.InventoryEntity;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.repository.InventoryRepository;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.service.InventoryService;
import com.gn.pharmacy.service.ProductService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto requestDto) throws Exception {
        logger.debug("Creating new product with name: {}", requestDto.getProductName());

        // Updated validation - checks per-size maps
        validateProductData(requestDto, true);

        // Check if SKU already exists
        if (requestDto.getSku() != null && !requestDto.getSku().trim().isEmpty()) {
            Optional<ProductEntity> existingProduct = productRepository.findAll()
                    .stream()
                    .filter(p -> p.getSku() != null && p.getSku().equals(requestDto.getSku()))
                    .findFirst();
            if (existingProduct.isPresent()) {
                throw new IllegalArgumentException("Product with SKU " + requestDto.getSku() + " already exists");
            }
        }

        ProductEntity entity = new ProductEntity();

        // Set basic fields
        entity.setSku(requestDto.getSku());
        entity.setProductName(requestDto.getProductName());
        entity.setProductCategory(requestDto.getProductCategory());
        entity.setProductSubCategory(requestDto.getProductSubCategory());
        entity.setProductPrice(requestDto.getProductPrice());
        entity.setProductOldPrice(requestDto.getProductOldPrice());
        entity.setProductStock(requestDto.getProductStock());
        entity.setProductStatus(requestDto.getProductStatus());
        entity.setProductDescription(requestDto.getProductDescription());
        entity.setPrescriptionRequired(requestDto.isPrescriptionRequired());
        entity.setBrandName(requestDto.getBrandName());
        entity.setRating(requestDto.getRating());
        entity.setBenefitsList(requestDto.getBenefitsList() != null ? requestDto.getBenefitsList() : new ArrayList<>());
        entity.setIngredientsList(requestDto.getIngredientsList() != null ? requestDto.getIngredientsList() : new ArrayList<>());
        entity.setDirectionsList(requestDto.getDirectionsList() != null ? requestDto.getDirectionsList() : new ArrayList<>());

        // Set category path
        if (requestDto.getCategoryPath() != null && !requestDto.getCategoryPath().isEmpty()) {
            entity.setCategoryPath(requestDto.getCategoryPath());
        } else {
            entity.setCategoryPath(buildCategoryPath(requestDto.getProductSubCategory()));
        }

        // Set main image
        if (requestDto.getProductMainImage() != null && !requestDto.getProductMainImage().isEmpty()) {
            entity.setProductMainImage(requestDto.getProductMainImage().getBytes());
        }

        // Set sub images
        if (requestDto.getProductSubImages() != null && !requestDto.getProductSubImages().isEmpty()) {
            List<byte[]> subImages = requestDto.getProductSubImages().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        try {
                            return file.getBytes();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to process sub image", e);
                        }
                    })
                    .collect(Collectors.toList());
            entity.setProductSubImages(subImages);
        } else {
            entity.setProductSubImages(new ArrayList<>());
        }

        // Set dynamic fields
        if (requestDto.getProductDynamicFields() != null) {
            entity.setProductDynamicFields(requestDto.getProductDynamicFields());
        } else {
            entity.setProductDynamicFields(new HashMap<>());
        }

        // Set sizes
        if (requestDto.getProductSizes() != null) {
            entity.setProductSizes(requestDto.getProductSizes());
        } else {
            entity.setProductSizes(new ArrayList<>());
        }

        entity.setCreatedAt(LocalDateTime.now());

        ProductEntity savedEntity = productRepository.save(entity);

        // ============== Automatically add initial inventory batch with variants =============
        BatchInfoDTO batchInfo = new BatchInfoDTO();
        batchInfo.setProductId(savedEntity.getProductId());
        batchInfo.setBatchNo(requestDto.getBatchNo() != null && !requestDto.getBatchNo().trim().isEmpty()
                ? requestDto.getBatchNo()
                : "INIT-PROD-" + savedEntity.getProductId());

        List<BatchInfoDTO.VariantDTO> variants = new ArrayList<>();

        List<String> sizes = savedEntity.getProductSizes();
        if (sizes != null && !sizes.isEmpty()) {
            // Preferred: per-size variants from DTO maps
            if (requestDto.getSizeQuantities() != null && !requestDto.getSizeQuantities().isEmpty()) {
                for (String size : sizes) {
                    Integer qty = requestDto.getSizeQuantities().getOrDefault(size, 0);
                    String mfg = (requestDto.getSizeMfgDates() != null) ? requestDto.getSizeMfgDates().getOrDefault(size, null) : null;
                    String exp = (requestDto.getSizeExpDates() != null) ? requestDto.getSizeExpDates().getOrDefault(size, null) : null;

                    if (qty > 0) {
                        variants.add(new BatchInfoDTO.VariantDTO(size, qty, mfg, exp));
                    }
                }
            }
        }

        // Fallback: single variant if no sizes or no per-size qty
        if (variants.isEmpty() && requestDto.getProductQuantity() != null && requestDto.getProductQuantity() > 0) {
            variants.add(new BatchInfoDTO.VariantDTO(
                    null, // no size
                    requestDto.getProductQuantity(),
                    requestDto.getMfgDate(),
                    requestDto.getExpDate()
            ));
        }

        if (!variants.isEmpty()) {
            batchInfo.setVariants(variants);
            inventoryService.addStockBatch(batchInfo);
            logger.info("Created batch with {} variants for Product {}", variants.size(), savedEntity.getProductId());
        } else {
            logger.warn("No initial stock added for Product {} - no quantity provided", savedEntity.getProductId());
        }

        logger.debug("Product saved with ID: {}", savedEntity.getProductId());
        return mapToResponseDto(savedEntity);
    }

    @Override
    public ProductResponseDto getProduct(Long productId) {
        logger.debug("Fetching product by ID: {}", productId);
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", productId);
                    return new IllegalArgumentException("Product not found with ID: " + productId);
                });
        return mapToResponseDto(entity);
    }

    @Override
    public Page<ProductResponseDto> getAllProducts(int page, int size) {
        logger.debug("Fetching all products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> productPage = productRepository.findAll(pageable);
        logger.debug("Found {} products on page {}", productPage.getNumberOfElements(), page);
        return productPage.map(this::mapToResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        try {
            Page<ProductEntity> entities = productRepository.findAllActive(pageable);
            return entities.map(this::mapToResponseDto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve products: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        try {
            List<ProductEntity> entities = productRepository.findAllActive();
            return entities.stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve products: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductResponseDto> getProductsByCategory(String category) {
        logger.debug("Fetching active & approved products by category: {}", category);

        List<ProductEntity> products = productRepository.findByProductCategoryAndActive(category);

        logger.debug("Found {} active & approved products for category: {}", products.size(), category);

        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getProductsBySubCategory(String subCategory) {
        logger.debug("Fetching active & approved products by sub category: {}", subCategory);

        List<ProductEntity> products = productRepository.findByProductSubCategoryAndActive(subCategory);

        logger.debug("Found {} active & approved products for sub category: {}", products.size(), subCategory);

        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) throws Exception {
        logger.debug("Updating product with ID: {}", id);

        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID: {}", id);
                    return new IllegalArgumentException("Product not found with ID: " + id);
                });

        validateProductData(requestDto, false);

        entity.setSku(requestDto.getSku());
        entity.setProductName(requestDto.getProductName());
        entity.setProductCategory(requestDto.getProductCategory());
        entity.setProductSubCategory(requestDto.getProductSubCategory());
        entity.setProductPrice(requestDto.getProductPrice());
        entity.setProductOldPrice(requestDto.getProductOldPrice());
        entity.setProductStock(requestDto.getProductStock());
        entity.setProductStatus(requestDto.getProductStatus());
        entity.setProductDescription(requestDto.getProductDescription());
        entity.setPrescriptionRequired(requestDto.isPrescriptionRequired());
        entity.setBrandName(requestDto.getBrandName());
        entity.setRating(requestDto.getRating());

        if (requestDto.getBenefitsList() != null) {
            entity.setBenefitsList(requestDto.getBenefitsList());
        }
        if (requestDto.getIngredientsList() != null) {
            entity.setIngredientsList(requestDto.getIngredientsList());
        }
        if (requestDto.getDirectionsList() != null) {
            entity.setDirectionsList(requestDto.getDirectionsList());
        }

        if (requestDto.getCategoryPath() != null) {
            entity.setCategoryPath(requestDto.getCategoryPath());
        } else if (requestDto.getProductSubCategory() != null) {
            entity.setCategoryPath(buildCategoryPath(requestDto.getProductSubCategory()));
        }

        if (requestDto.getProductMainImage() != null && !requestDto.getProductMainImage().isEmpty()) {
            entity.setProductMainImage(requestDto.getProductMainImage().getBytes());
        }

        if (requestDto.getProductSubImages() != null) {
            List<byte[]> subImages = requestDto.getProductSubImages().stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        try {
                            return file.getBytes();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to process sub image", e);
                        }
                    })
                    .collect(Collectors.toList());
            entity.setProductSubImages(subImages);
        }

        if (requestDto.getProductDynamicFields() != null) {
            entity.setProductDynamicFields(requestDto.getProductDynamicFields());
        }

        if (requestDto.getProductSizes() != null) {
            entity.setProductSizes(requestDto.getProductSizes());
        }

        ProductEntity updatedEntity = productRepository.save(entity);
        logger.debug("Product updated successfully with ID: {}", id);
        return mapToResponseDto(updatedEntity);
    }

    @Override
    @Transactional
    public ProductResponseDto patchProduct(
            Long id,
            ProductPatchDto patchDto,
            MultipartFile productMainImage,
            List<MultipartFile> productSubImages) throws Exception {

        logger.debug("Patching product with ID: {}", id);

        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));

        // PATCH SCALAR FIELDS
        if (patchDto.getSku() != null) entity.setSku(patchDto.getSku());
        if (patchDto.getProductName() != null) entity.setProductName(patchDto.getProductName());
        if (patchDto.getProductCategory() != null) entity.setProductCategory(patchDto.getProductCategory());
        if (patchDto.getProductSubCategory() != null) {
            entity.setProductSubCategory(patchDto.getProductSubCategory());
            entity.setCategoryPath(buildCategoryPath(patchDto.getProductSubCategory()));
        }
        if (patchDto.getProductStock() != null) entity.setProductStock(patchDto.getProductStock());
        if (patchDto.getProductStatus() != null) entity.setProductStatus(patchDto.getProductStatus());
        if (patchDto.getProductDescription() != null) entity.setProductDescription(patchDto.getProductDescription());

        if (patchDto.getRating() != null) entity.setRating(patchDto.getRating());

        // Boolean fields - only update if explicitly set
        if (patchDto.getApproved() != null) {
            entity.setApproved(patchDto.getApproved());
        }
        if (patchDto.getPrescriptionRequired() != null) {
            entity.setPrescriptionRequired(patchDto.getPrescriptionRequired());
        }

        // PATCH LIST FIELDS (safe - only if non-null and non-empty)
        if (patchDto.getProductPrice() != null && !patchDto.getProductPrice().isEmpty()) {
            entity.setProductPrice(patchDto.getProductPrice());
        }
        if (patchDto.getProductOldPrice() != null && !patchDto.getProductOldPrice().isEmpty()) {
            entity.setProductOldPrice(patchDto.getProductOldPrice());
        }
        if (patchDto.getProductSizes() != null && !patchDto.getProductSizes().isEmpty()) {
            entity.setProductSizes(patchDto.getProductSizes());
        }
        if (patchDto.getBenefitsList() != null && !patchDto.getBenefitsList().isEmpty()) {
            entity.setBenefitsList(patchDto.getBenefitsList());
        }
        if (patchDto.getIngredientsList() != null && !patchDto.getIngredientsList().isEmpty()) {
            entity.setIngredientsList(patchDto.getIngredientsList());
        }
        if (patchDto.getDirectionsList() != null && !patchDto.getDirectionsList().isEmpty()) {
            entity.setDirectionsList(patchDto.getDirectionsList());
        }
        if (patchDto.getCategoryPath() != null && !patchDto.getCategoryPath().isEmpty()) {
            entity.setCategoryPath(patchDto.getCategoryPath());
        }
        if (patchDto.getProductDynamicFields() != null) {
            entity.setProductDynamicFields(patchDto.getProductDynamicFields());
        }

        // PATCH IMAGES
        if (productMainImage != null && !productMainImage.isEmpty()) {
            entity.setProductMainImage(productMainImage.getBytes());
        }

        if (productSubImages != null && !productSubImages.isEmpty()) {
            List<byte[]> subImages = productSubImages.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        try {
                            return file.getBytes();
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to read sub image", e);
                        }
                    })
                    .collect(Collectors.toList());
            entity.setProductSubImages(subImages);
        }

        ProductEntity updatedEntity = productRepository.save(entity);
        logger.debug("Product patched successfully with ID: {}", id);

        return mapToResponseDto(updatedEntity);
    }

    private void validateProductData(ProductRequestDto dto, boolean isCreate) {
        if (dto.getProductName() == null || dto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }

        if (dto.getProductPrice() == null || dto.getProductPrice().isEmpty()) {
            throw new IllegalArgumentException("At least one product price is required");
        }

        if (dto.getProductPrice().stream().anyMatch(price -> price == null || price.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new IllegalArgumentException("All product prices must be greater than zero");
        }

        // Main image required only on create
        if (isCreate && (dto.getProductMainImage() == null || dto.getProductMainImage().isEmpty())) {
            throw new IllegalArgumentException("Product main image is required when creating a product");
        }

        // Sizes and prices consistency
        List<String> sizes = dto.getProductSizes();
        List<BigDecimal> prices = dto.getProductPrice();
        List<BigDecimal> oldPrices = dto.getProductOldPrice();

        if (sizes != null && !sizes.isEmpty()) {
            if (prices.size() != sizes.size()) {
                throw new IllegalArgumentException("Number of prices must match number of sizes");
            }
            if (oldPrices != null && !oldPrices.isEmpty() && oldPrices.size() != sizes.size()) {
                throw new IllegalArgumentException("Number of old prices must match number of sizes");
            }

            // Validate old > current where provided
            if (oldPrices != null) {
                for (int i = 0; i < prices.size(); i++) {
                    BigDecimal current = prices.get(i);
                    BigDecimal old = oldPrices.get(i);
                    if (old != null && old.compareTo(current) <= 0) {
                        throw new IllegalArgumentException("Old price must be greater than current price for size: " + sizes.get(i));
                    }
                }
            }

            // NEW: Validate per-size data if sizes exist
            if (dto.getSizeQuantities() == null || dto.getSizeQuantities().size() != sizes.size()) {
                throw new IllegalArgumentException("sizeQuantities must have an entry for every size");
            }
            // mfg/exp are optional → no strict size check, but log if missing
            if (dto.getSizeMfgDates() != null && dto.getSizeMfgDates().size() != sizes.size()) {
                logger.warn("sizeMfgDates provided but size mismatch - some dates may be missing");
            }
            if (dto.getSizeExpDates() != null && dto.getSizeExpDates().size() != sizes.size()) {
                logger.warn("sizeExpDates provided but size mismatch - some dates may be missing");
            }

            // Check qty non-negative
            for (String size : sizes) {
                Integer qty = dto.getSizeQuantities().get(size);
                if (qty == null || qty < 0) {
                    throw new IllegalArgumentException("Quantity for size " + size + " must be non-negative");
                }
            }
        } else {
            // No sizes → require single quantity
            if (dto.getProductQuantity() == null || dto.getProductQuantity() < 0) {
                throw new IllegalArgumentException("Product quantity must be non-negative when no sizes are provided");
            }
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        logger.debug("Deleting product with ID: {}", productId);
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }
        productRepository.deleteById(productId);
        logger.debug("Product deleted successfully with ID: {}", productId);
    }

    @Override
    public List<ProductResponseDto> getProductsByCategoryPath(List<String> path) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }
        List<ProductEntity> products = productRepository.findByCategoryPath(path, path.size());
        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getProductsBySubPath(String subPath) {
        if (subPath == null || subPath.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<ProductEntity> products = productRepository.findByCategoryPathContaining(subPath.trim());
        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BulkUploadResponse bulkCreateProducts(MultipartFile excelFile, List<MultipartFile> images) throws Exception {
        logger.debug("Starting bulk product creation from Excel");

        Map<String, MultipartFile> imageMap = new HashMap<>();
        if (images != null) {
            for (MultipartFile image : images) {
                String fullFilename = image.getOriginalFilename();
                if (fullFilename != null) {
                    String baseName = fullFilename.contains(".")
                            ? fullFilename.substring(0, fullFilename.lastIndexOf('.'))
                            : fullFilename;
                    imageMap.put(baseName.trim().toLowerCase(), image);
                }
            }
        }

        int uploadedCount = 0;
        int skippedCount = 0;
        List<String> skippedReasons = new ArrayList<>();

        try (InputStream is = excelFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next(); // Skip header

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                String productName = getCellValue(row.getCell(0)).trim();
                if (productName.isEmpty()) {
                    skippedCount++;
                    skippedReasons.add("Empty product name at row " + (row.getRowNum() + 1));
                    continue;
                }

                if (productRepository.existsByProductName(productName)) {
                    skippedCount++;
                    skippedReasons.add("Duplicate product name: " + productName);
                    continue;
                }

                ProductRequestDto dto = new ProductRequestDto();

                dto.setProductName(productName);
                dto.setProductCategory(getCellValue(row.getCell(1)));
                dto.setProductSubCategory(getCellValue(row.getCell(2)));
                dto.setSku(getCellValue(row.getCell(21)));

                // Prices
                List<BigDecimal> prices = new ArrayList<>();
                String priceStr = getCellValue(row.getCell(3)).trim();
                if (priceStr.isEmpty()) {
                    skippedCount++;
                    skippedReasons.add("Missing price for product: " + productName);
                    continue;
                }
                try {
                    for (String p : priceStr.split(",")) {
                        String clean = p.trim().replace("\u00A0", "");
                        if (!clean.isEmpty()) prices.add(new BigDecimal(clean));
                    }
                } catch (Exception e) {
                    skippedCount++;
                    skippedReasons.add("Invalid price for product: " + productName);
                    continue;
                }
                dto.setProductPrice(prices);

                // Old prices (optional)
                List<BigDecimal> oldPrices = new ArrayList<>();
                String oldPriceStr = getCellValue(row.getCell(4));
                if (!oldPriceStr.isEmpty()) {
                    try {
                        for (String p : oldPriceStr.split(",")) {
                            String clean = p.trim().replace("\u00A0", "");
                            if (!clean.isEmpty()) oldPrices.add(new BigDecimal(clean));
                        }
                    } catch (Exception ignored) {
                        // Ignore invalid old prices
                    }
                }
                dto.setProductOldPrice(oldPrices);

                // Sizes
                List<String> sizes = new ArrayList<>();
                String sizeStr = getCellValue(row.getCell(12));
                if (!sizeStr.isEmpty()) {
                    sizes = Arrays.stream(sizeStr.split(","))
                            .map(String::trim)
                            .map(s -> s.replace("\u00A0", ""))
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
                dto.setProductSizes(sizes);

                // Auto-apply single price to all sizes
                if (sizes.size() > 1 && prices.size() == 1) {
                    BigDecimal single = prices.get(0);
                    prices = new ArrayList<>();
                    for (int i = 0; i < sizes.size(); i++) prices.add(single);
                    dto.setProductPrice(prices);

                    if (oldPrices.size() == 1) {
                        BigDecimal singleOld = oldPrices.get(0);
                        oldPrices = new ArrayList<>();
                        for (int i = 0; i < sizes.size(); i++) oldPrices.add(singleOld);
                        dto.setProductOldPrice(oldPrices);
                    }
                }

                // Validate size-price mapping
                try {
                    validateSizePriceMapping(sizes, prices, oldPrices, productName);
                } catch (IllegalArgumentException e) {
                    skippedCount++;
                    skippedReasons.add("Error creating product " + productName + ": " + e.getMessage());
                    continue;
                }

                // Quantity (fallback)
                Integer quantity = getIntegerCellValue(row.getCell(8));
                if (quantity == null) {
                    skippedCount++;
                    skippedReasons.add("Invalid quantity for product: " + productName);
                    continue;
                }
                dto.setProductQuantity(quantity);

                // Main image
                String mainImageName = getCellValue(row.getCell(9)).trim();
                if (!mainImageName.isEmpty()) {
                    MultipartFile mainImage = imageMap.get(mainImageName.toLowerCase());
                    if (mainImage != null) {
                        dto.setProductMainImage(mainImage);
                    } else {
                        skippedCount++;
                        skippedReasons.add("Missing main image: " + productName);
                        continue;
                    }
                }

                // Sub images
                String subImagesStr = getCellValue(row.getCell(10));
                List<MultipartFile> subImages = new ArrayList<>();
                if (!subImagesStr.isEmpty()) {
                    boolean missing = false;
                    for (String name : subImagesStr.split(",")) {
                        MultipartFile img = imageMap.get(name.trim().toLowerCase());
                        if (img != null) subImages.add(img);
                        else {
                            missing = true;
                            skippedReasons.add("Missing sub image: " + name);
                        }
                    }
                    if (missing) {
                        skippedCount++;
                        continue;
                    }
                }
                dto.setProductSubImages(subImages);

                // Dynamic fields
                Map<String, String> dynamicFields = new HashMap<>();
                String dynamicStr = getCellValue(row.getCell(11));
                if (!dynamicStr.isEmpty()) {
                    for (String pair : dynamicStr.split(",")) {
                        String[] kv = pair.split(":");
                        if (kv.length == 2) dynamicFields.put(kv[0].trim(), kv[1].trim());
                    }
                }
                dto.setProductDynamicFields(dynamicFields);

                // Medical & brand
                dto.setPrescriptionRequired(Boolean.TRUE.equals(getBooleanCellValue(row.getCell(13))));
                dto.setBrandName(getCellValue(row.getCell(14)));

                // Per-size data (if Excel supports it - extend columns as needed)
                // For now, fallback to single quantity - extend later if needed
                // dto.setSizeQuantities(...); // parse from additional columns

                dto.setMfgDate(getCellValue(row.getCell(15)));
                dto.setExpDate(getCellValue(row.getCell(16)));
                dto.setBatchNo(getCellValue(row.getCell(17)));

                dto.setBenefitsList(parseCommaList(row.getCell(18)));
                dto.setDirectionsList(parseCommaList(row.getCell(19)));
                dto.setIngredientsList(parseCommaList(row.getCell(20)));

                try {
                    createProduct(dto);
                    uploadedCount++;
                } catch (Exception e) {
                    skippedCount++;
                    skippedReasons.add("Error creating product " + productName + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Bulk upload failed: " + e.getMessage(), e);
        }

        BulkUploadResponse response = new BulkUploadResponse();
        response.setUploadedCount(uploadedCount);
        response.setSkippedCount(skippedCount);
        response.setSkippedReasons(skippedReasons);
        return response;
    }

    // Helpers (unchanged)
    private List<String> parseCommaList(Cell cell) {
        String value = getCellValue(cell);
        if (value == null || value.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private void validateSizePriceMapping(List<String> sizes, List<BigDecimal> prices,
                                          List<BigDecimal> oldPrices, String productName) {
        if (sizes != null && !sizes.isEmpty()) {
            if (prices == null || sizes.size() != prices.size()) {
                throw new IllegalArgumentException("Sizes count must match price count for product: " + productName);
            }
            if (oldPrices != null && !oldPrices.isEmpty() && oldPrices.size() != prices.size()) {
                throw new IllegalArgumentException("Old price count must match price count for product: " + productName);
            }
        }
    }

    private Integer getIntegerCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case STRING:
                    String strVal = cell.getStringCellValue().trim();
                    if (strVal.isEmpty()) return null;
                    return Integer.parseInt(strVal);
                case NUMERIC:
                    double numVal = cell.getNumericCellValue();
                    if (numVal == Math.floor(numVal)) return (int) numVal;
                    logger.warn("Non-integer numeric value found: {}", numVal);
                    return null;
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? 1 : 0;
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse integer from cell: {}", cell);
            return null;
        }
    }

    private Boolean getBooleanCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case STRING:
                    String strVal = cell.getStringCellValue().trim().toLowerCase();
                    if (strVal.isEmpty()) return null;
                    return switch (strVal) {
                        case "true", "yes", "1" -> true;
                        case "false", "no", "0" -> false;
                        default -> null;
                    };
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case NUMERIC:
                    double numVal = cell.getNumericCellValue();
                    return numVal == 1.0;
                default:
                    return null;
            }
        } catch (Exception e) {
            logger.warn("Failed to parse boolean from cell: {}", cell);
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private List<String> buildCategoryPath(String subCategory) {
        if (subCategory == null || subCategory.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(subCategory.split(">"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private ProductResponseDto mapToResponseDto(ProductEntity entity) {
        ProductResponseDto responseDto = new ProductResponseDto();

        responseDto.setApproved(entity.isApproved());
        responseDto.setDeleted(entity.isDeleted());
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
        responseDto.setPrescriptionRequired(entity.isPrescriptionRequired());
        responseDto.setBrandName(entity.getBrandName());
        responseDto.setRating(entity.getRating());
        responseDto.setBenefitsList(entity.getBenefitsList() != null ? entity.getBenefitsList() : new ArrayList<>());
        responseDto.setIngredientsList(entity.getIngredientsList() != null ? entity.getIngredientsList() : new ArrayList<>());
        responseDto.setDirectionsList(entity.getDirectionsList() != null ? entity.getDirectionsList() : new ArrayList<>());
        responseDto.setCategoryPath(entity.getCategoryPath() != null ? entity.getCategoryPath() : new ArrayList<>());

        // Stock by size (from variants across all batches)
        Map<String, Integer> stockBySize = new HashMap<>();
        int totalQuantity = 0;

        List<InventoryEntity> inventories = inventoryRepository.findByProduct(entity);
        for (InventoryEntity inv : inventories) {
            if (inv.getVariants() != null) {
                for (BatchVariant v : inv.getVariants()) {
                    String sizeKey = v.getSize() != null ? v.getSize() : "DEFAULT";
                    int qty = v.getQuantity() != null ? v.getQuantity() : 0;
                    stockBySize.merge(sizeKey, qty, Integer::sum);
                    totalQuantity += qty;
                }
            }
        }

        responseDto.setStockBySize(stockBySize);
        responseDto.setProductQuantity(totalQuantity);

        // Images
        if (entity.getProductMainImage() != null) {
            responseDto.setProductMainImage("/api/products/" + entity.getProductId() + "/image");
        }

        if (entity.getProductSubImages() != null && !entity.getProductSubImages().isEmpty()) {
            List<String> subImageUrls = IntStream.range(0, entity.getProductSubImages().size())
                    .mapToObj(i -> "/api/products/" + entity.getProductId() + "/subimage/" + i)
                    .collect(Collectors.toList());
            responseDto.setProductSubImages(subImageUrls);
        }

        responseDto.setProductDynamicFields(entity.getProductDynamicFields());
        responseDto.setProductSizes(entity.getProductSizes() != null ? entity.getProductSizes() : new ArrayList<>());

        return responseDto;
    }
}