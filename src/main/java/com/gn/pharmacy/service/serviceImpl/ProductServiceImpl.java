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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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



//    @Override
//    public BulkUploadResponse bulkCreateProducts(MultipartFile excelFile, List<MultipartFile> images) throws Exception {
//        logger.debug("Starting bulk product creation from Excel");
//
//        Map<String, MultipartFile> imageMap = new HashMap<>();
//        if (images != null) {
//            for (MultipartFile image : images) {
//                String name = image.getOriginalFilename();
//                if (name != null) {
//                    String base = name.contains(".") ? name.substring(0, name.lastIndexOf(".")) : name;
//                    imageMap.put(base.trim().toLowerCase(), image);
//                }
//            }
//        }
//
//        int uploadedCount = 0;
//        int skippedCount = 0;
//        List<String> skippedReasons = new ArrayList<>();
//
//        try (InputStream is = excelFile.getInputStream();
//             Workbook workbook = new XSSFWorkbook(is)) {
//
//            Sheet sheet = workbook.getSheetAt(0);
//            Iterator<Row> iterator = sheet.iterator();
//            if (iterator.hasNext()) iterator.next(); // header
//
//            while (iterator.hasNext()) {
//                Row row = iterator.next();
//
//                String productName = getCellValue(row.getCell(0)).trim();
//                if (productName.isEmpty()) {
//                    skippedCount++;
//                    skippedReasons.add("Empty product name at row " + (row.getRowNum() + 1));
//                    continue;
//                }
//
//                if (productRepository.existsByProductName(productName)) {
//                    skippedCount++;
//                    skippedReasons.add("Duplicate product: " + productName);
//                    continue;
//                }
//
//                ProductRequestDto dto = new ProductRequestDto();
//                dto.setProductName(productName);
//                dto.setProductCategory(getCellValue(row.getCell(1)));
//                dto.setProductSubCategory(getCellValue(row.getCell(2)));
//                dto.setSku(getCellValue(row.getCell(21)));
//
//                // ────────────── SIZES ──────────────
//                List<String> sizes = parseCommaList(row.getCell(12));
//                dto.setProductSizes(sizes);
//
//                // ────────────── PRICES ──────────────
//                List<BigDecimal> prices = parseDecimalList(row.getCell(3));
//                List<BigDecimal> oldPrices = parseDecimalList(row.getCell(4));
//                autoExpand(sizes, prices, "price", productName);
//                autoExpand(sizes, oldPrices, "old price", productName);
//                dto.setProductPrice(prices);
//                dto.setProductOldPrice(oldPrices);
//
//                // ────────────── QUANTITIES → Map<String, Integer> ──────────────
//                List<Integer> qtyList = parseIntegerList(row.getCell(22));
//                autoExpand(sizes, qtyList, "quantity", productName);
//
//                Map<String, Integer> sizeQuantities = new LinkedHashMap<>();
//                for (int i = 0; i < sizes.size(); i++) {
//                    sizeQuantities.put(sizes.get(i), qtyList.get(i));
//                }
//                dto.setSizeQuantities(sizeQuantities);
//
//                // ────────────── MFG DATES → Map<String, String> ──────────────
//                List<String> mfgList = parseCommaList(row.getCell(15));
//                autoExpand(sizes, mfgList, "mfg date", productName);
//
//                Map<String, String> sizeMfgDates = new LinkedHashMap<>();
//                for (int i = 0; i < sizes.size(); i++) {
//                    sizeMfgDates.put(sizes.get(i), mfgList.get(i));
//                }
//                dto.setSizeMfgDates(sizeMfgDates);
//
//                // ────────────── EXP DATES → Map<String, String> ──────────────
//                List<String> expList = parseCommaList(row.getCell(16));
//                autoExpand(sizes, expList, "exp date", productName);
//
//                Map<String, String> sizeExpDates = new LinkedHashMap<>();
//                for (int i = 0; i < sizes.size(); i++) {
//                    sizeExpDates.put(sizes.get(i), expList.get(i));
//                }
//                dto.setSizeExpDates(sizeExpDates);
//
//                // ────────────── BATCH NOS (optional – you can keep as list or map) ──────────────
//                List<String> batchNos = parseCommaList(row.getCell(17));
//                autoExpand(sizes, batchNos, "batch no", productName);
//
//                // Option 1: keep as single string (joined) – as you had
//                dto.setBatchNo(String.join(", ", batchNos));
//
//                // ────────────── IMAGES ──────────────
//                String mainImageName = getCellValue(row.getCell(9)).trim();
//                MultipartFile mainImage = imageMap.get(mainImageName.toLowerCase());
//                if (mainImage == null && !mainImageName.isEmpty()) {
//                    skippedCount++;
//                    skippedReasons.add("Missing main image: " + productName);
//                    continue;
//                }
//                if (mainImage != null) {
//                    dto.setProductMainImage(mainImage);
//                }
//
//                List<MultipartFile> subImages = new ArrayList<>();
//                for (String img : parseCommaList(row.getCell(10))) {
//                    MultipartFile f = imageMap.get(img.trim().toLowerCase());
//                    if (f != null) {
//                        subImages.add(f);
//                    } else if (!img.trim().isEmpty()) {
//                        skippedReasons.add("Missing sub image: " + img + " for " + productName);
//                    }
//                }
//                if (!subImages.isEmpty()) {
//                    dto.setProductSubImages(subImages);
//                }
//
//                // ────────────── OTHER FIELDS ──────────────
//                dto.setPrescriptionRequired(getBooleanCellValue(row.getCell(13)));
//                dto.setBrandName(getCellValue(row.getCell(14)));
//                dto.setBenefitsList(parseCommaList(row.getCell(18)));
//                dto.setDirectionsList(parseCommaList(row.getCell(19)));
//                dto.setIngredientsList(parseCommaList(row.getCell(20)));
//
//                try {
//                    createProduct(dto);
//                    uploadedCount++;
//                } catch (Exception e) {
//                    skippedCount++;
//                    skippedReasons.add("Failed product '" + productName + "': " + e.getMessage());
//                }
//            }
//        }
//
//        BulkUploadResponse response = new BulkUploadResponse();
//        response.setUploadedCount(uploadedCount);
//        response.setSkippedCount(skippedCount);
//        response.setSkippedReasons(skippedReasons);
//        return response;
//    }
//
//
//    private <T> void autoExpand(List<String> sizes, List<T> values, String field, String product) {
//        if (sizes == null || sizes.isEmpty()) return;
//        if (values == null || values.isEmpty())
//            throw new IllegalArgumentException("Missing " + field + " for " + product);
//
//        if (values.size() == 1 && sizes.size() > 1) {
//            T single = values.get(0);
//            values.clear();
//            for (int i = 0; i < sizes.size(); i++) values.add(single);
//        }
//
//        if (values.size() != sizes.size())
//            throw new IllegalArgumentException(field + " count mismatch for " + product);
//    }
//
//    private List<BigDecimal> parseDecimalList(Cell cell) {
//        List<BigDecimal> list = new ArrayList<>();
//        for (String v : parseCommaList(cell)) list.add(new BigDecimal(v));
//        return list;
//    }
//
//    private List<Integer> parseIntegerList(Cell cell) {
//        List<Integer> list = new ArrayList<>();
//        for (String v : parseCommaList(cell)) list.add(Integer.parseInt(v));
//        return list;
//    }
//
//    // Helpers (unchanged)
//    private List<String> parseCommaList(Cell cell) {
//        String value = getCellValue(cell);
//        if (value == null || value.trim().isEmpty()) return new ArrayList<>();
//        return Arrays.stream(value.split(","))
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .collect(Collectors.toList());
//    }
//
//
//
//
//    private Boolean getBooleanCellValue(Cell cell) {
//        if (cell == null) return null;
//        try {
//            switch (cell.getCellType()) {
//                case STRING:
//                    String strVal = cell.getStringCellValue().trim().toLowerCase();
//                    if (strVal.isEmpty()) return null;
//                    return switch (strVal) {
//                        case "true", "yes", "1" -> true;
//                        case "false", "no", "0" -> false;
//                        default -> null;
//                    };
//                case BOOLEAN:
//                    return cell.getBooleanCellValue();
//                case NUMERIC:
//                    double numVal = cell.getNumericCellValue();
//                    return numVal == 1.0;
//                default:
//                    return null;
//            }
//        } catch (Exception e) {
//            logger.warn("Failed to parse boolean from cell: {}", cell);
//            return null;
//        }
//    }
//
//    private String getCellValue(Cell cell) {
//        if (cell == null) return "";
//        switch (cell.getCellType()) {
//            case STRING:
//                return cell.getStringCellValue().trim();
//            case NUMERIC:
//                return String.valueOf(cell.getNumericCellValue());
//            case BOOLEAN:
//                return String.valueOf(cell.getBooleanCellValue());
//            default:
//                return "";
//        }
//    }
//
//
//    private Integer getIntegerCellValue(Cell cell) {
//        if (cell == null) return null;
//        try {
//            switch (cell.getCellType()) {
//                case STRING:
//                    String strVal = cell.getStringCellValue().trim();
//                    if (strVal.isEmpty()) return null;
//                    return Integer.parseInt(strVal);
//                case NUMERIC:
//                    double numVal = cell.getNumericCellValue();
//                    if (numVal == Math.floor(numVal)) return (int) numVal;
//                    logger.warn("Non-integer numeric value found: {}", numVal);
//                    return null;
//                case BOOLEAN:
//                    return cell.getBooleanCellValue() ? 1 : 0;
//                default:
//                    return null;
//            }
//        } catch (NumberFormatException e) {
//            logger.warn("Failed to parse integer from cell: {}", cell);
//            return null;
//        }
//    }


    @Override
    public BulkUploadResponse bulkCreateProducts(MultipartFile excelFile, List<MultipartFile> images) throws Exception {
        logger.info("╔══════════════════════════════════════════════════════════════════════════════╗");
        logger.info("║              BULK PRODUCT UPLOAD STARTED ── {}", Instant.now());
        logger.info("╚══════════════════════════════════════════════════════════════════════════════╝");

        // ─── Image map preparation ───
        Map<String, MultipartFile> imageMap = new HashMap<>();
        if (images != null && !images.isEmpty()) {
            logger.debug("Received {} product images", images.size());
            for (MultipartFile image : images) {
                String filename = image.getOriginalFilename();
                if (filename != null && !filename.trim().isEmpty()) {
                    String key = normalizeImageKey(filename);
                    imageMap.put(key, image);
                    logger.trace("Mapped image: {} → key={}", filename, key);
                }
            }
            logger.info("Successfully prepared {} image(s) for upload", imageMap.size());
        } else {
            logger.info("No product images provided");
        }

        int uploadedCount = 0;
        int skippedCount = 0;
        List<String> skippedReasons = new ArrayList<>();

        try (InputStream is = excelFile.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                logger.error("Excel file has no header row ── upload aborted");
                throw new IllegalArgumentException("Excel file has no header row");
            }

            // ─── Column name → index mapping ───
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String header = getCellValue(headerRow.getCell(i))
                        .trim()
                        .toLowerCase()
                        .replaceAll("\\s+", " ");
                if (!header.isEmpty()) {
                    colIndex.put(header, i);
                }
            }

            logger.info("Detected {} columns in Excel sheet", colIndex.size());
            logger.debug("Column mapping: {}", colIndex);

            // Required columns validation
            String[] required = {
                    "product name", "category", "sub category", "prices", "old prices",
                    "sizes", "variant quantities"
            };
            List<String> missing = Arrays.stream(required)
                    .filter(req -> !colIndex.containsKey(req))
                    .toList();

            if (!missing.isEmpty()) {
                String msg = "Missing required column(s): " + String.join(", ", missing);
                logger.error(msg);
                throw new IllegalArgumentException(msg);
            }

            Iterator<Row> iterator = sheet.rowIterator();
            iterator.next(); // skip header

            int rowNumber = 1;

            while (iterator.hasNext()) {
                Row row = iterator.next();
                rowNumber++;

                String productName = getCellValue(row.getCell(colIndex.get("product name"))).trim();
                if (productName.isEmpty()) {
                    skippedCount++;
                    String reason = "Skipped ── empty product name (row " + rowNumber + ")";
                    skippedReasons.add(reason);
                    logger.warn(reason);
                    continue;
                }

                logger.info("Processing row {} ── Product: {}", rowNumber, productName);

                if (productRepository.existsByProductName(productName)) {
                    skippedCount++;
                    String reason = "Skipped ── duplicate product name: " + productName + " (row " + rowNumber + ")";
                    skippedReasons.add(reason);
                    logger.warn(reason);
                    continue;
                }

                ProductRequestDto dto = new ProductRequestDto();
                dto.setProductName(productName);
                dto.setProductCategory(getCellValue(row.getCell(colIndex.get("category"))));
                dto.setProductSubCategory(getCellValue(row.getCell(colIndex.get("sub category"))));
                dto.setSku(getCellValue(row.getCell(colIndex.get("sku"))));

                // Sizes
                List<String> sizes = parseSemicolonList(row.getCell(colIndex.get("sizes")));
                dto.setProductSizes(sizes);

                // Prices & Old Prices
                List<BigDecimal> prices = parseDecimalList(row.getCell(colIndex.get("prices")));
                List<BigDecimal> oldPrices = parseDecimalList(row.getCell(colIndex.get("old prices")));
                autoExpand(sizes, prices, "price", productName);
                autoExpand(sizes, oldPrices, "old price", productName);
                dto.setProductPrice(prices);
                dto.setProductOldPrice(oldPrices);

                // Variant Quantities
                List<Integer> qtyList = parseIntegerList(row.getCell(colIndex.get("variant quantities")));
                autoExpand(sizes, qtyList, "quantity", productName);
                Map<String, Integer> sizeQuantities = new LinkedHashMap<>();
                for (int i = 0; i < sizes.size(); i++) {
                    sizeQuantities.put(sizes.get(i), qtyList.get(i));
                }
                dto.setSizeQuantities(sizeQuantities);

                // Optional fields with defaults
                dto.setPrescriptionRequired(getOptionalBoolean(row, colIndex, "prescription", false));
                dto.setBrandName(getOptionalString(row, colIndex, "brand", null));

                dto.setSizeMfgDates(buildOptionalSizeMap(sizes, parseSemicolonList(row.getCell(colIndex.get("mfg dates")))));
                dto.setSizeExpDates(buildOptionalSizeMap(sizes, parseSemicolonList(row.getCell(colIndex.get("exp dates")))));

                // Batch No – take first non-empty value (no duplication)
                String batchNo = "";
                List<String> batchList = parseSemicolonList(row.getCell(colIndex.get("batch no")));
                if (!batchList.isEmpty()) {
                    batchNo = batchList.stream()
                            .filter(s -> !s.trim().isEmpty())
                            .findFirst()
                            .orElse("");
                }
                dto.setBatchNo(batchNo);

                dto.setBenefitsList(parseSemicolonList(row.getCell(colIndex.get("benefits"))));
                dto.setDirectionsList(parseSemicolonList(row.getCell(colIndex.get("directions"))));
                dto.setIngredientsList(parseSemicolonList(row.getCell(colIndex.get("ingredients"))));

                dto.setProductDynamicFields(parseDynamicFields(row, colIndex));

                // IMPORTANT: Default approval for bulk upload
                dto.setApproved(true);

                // ─── Images ───
                String mainImageName = getCellValue(row.getCell(colIndex.get("main image"))).trim();
                MultipartFile mainImage = mainImageName.isEmpty() ? null : imageMap.get(normalizeImageKey(mainImageName));
                if (mainImage == null && !mainImageName.isEmpty()) {
                    String reason = "Skipped ── missing main image '" + mainImageName + "' for " + productName;
                    skippedCount++;
                    skippedReasons.add(reason);
                    logger.warn(reason);
                    continue;
                }
                dto.setProductMainImage(mainImage);

                List<MultipartFile> subImages = new ArrayList<>();
                for (String imgName : parseSemicolonList(row.getCell(colIndex.get("sub images")))) {
                    String key = normalizeImageKey(imgName);
                    MultipartFile f = imageMap.get(key);
                    if (f != null) {
                        subImages.add(f);
                    } else if (!imgName.trim().isEmpty()) {
                        String reason = "Missing sub image '" + imgName + "' for " + productName;
                        skippedReasons.add(reason);
                        logger.warn(reason);
                    }
                }
                dto.setProductSubImages(subImages);

                // Debug final DTO state before creation
                logger.debug("Creating product '{}' | approved={} | sizes={} | prices={} | batch='{}'",
                        productName, dto.isApproved(), dto.getProductSizes(), dto.getProductPrice(), dto.getBatchNo());

                // Create product
                try {
                    createProduct(dto);
                    uploadedCount++;
                    logger.info("→ SUCCESS: Product created ── {} (row {})", productName, rowNumber);
                } catch (Exception e) {
                    skippedCount++;
                    String reason = "Failed to create '" + productName + "' (row " + rowNumber + "): " + e.getMessage();
                    skippedReasons.add(reason);
                    logger.error("→ FAILURE: {}", reason, e);
                }
            }

        } catch (Exception e) {
            logger.error("CRITICAL ERROR during bulk upload processing ── aborting", e);
            throw e;
        }

        logger.info("╔══════════════════════════════════════════════════════════════════════════════╗");
        logger.info("║              BULK PRODUCT UPLOAD FINISHED ── {}", Instant.now());
        logger.info("║  Uploaded: {}    Skipped: {}    Total processed: {}",
                uploadedCount, skippedCount, (uploadedCount + skippedCount));
        logger.info("╚══════════════════════════════════════════════════════════════════════════════╝");

        BulkUploadResponse response = new BulkUploadResponse();
        response.setUploadedCount(uploadedCount);
        response.setSkippedCount(skippedCount);
        response.setSkippedReasons(skippedReasons);

        return response;
    }

    // ────────────────────────────────────────────────────────────────────────────────
//  NEW: Unified semicolon-based list parser (used everywhere)
// ────────────────────────────────────────────────────────────────────────────────
    private List<String> parseSemicolonList(Cell cell) {
        String value = getCellValue(cell);
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────────────────────────────────────────────
//  Updated parseDecimalList & parseIntegerList to use semicolon parser
// ────────────────────────────────────────────────────────────────────────────────
    private List<BigDecimal> parseDecimalList(Cell cell) {
        List<BigDecimal> list = new ArrayList<>();
        for (String v : parseSemicolonList(cell)) {
            String cleaned = v.trim().replaceAll("[^0-9.-]", "");
            if (cleaned.isEmpty()) continue;
            try {
                list.add(new BigDecimal(cleaned));
            } catch (NumberFormatException e) {
                logger.warn("Invalid decimal skipped: '{}' (row {})", v, cell.getRowIndex() + 1);
            }
        }
        return list;
    }

    private List<Integer> parseIntegerList(Cell cell) {
        List<Integer> list = new ArrayList<>();
        for (String v : parseSemicolonList(cell)) {
            String cleaned = v.trim();
            if (cleaned.isEmpty()) continue;
            try {
                list.add(Integer.parseInt(cleaned));
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer skipped: '{}' (row {})", v, cell.getRowIndex() + 1);
            }
        }
        return list;
    }

    private List<String> getOptionalList(Row row, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName);
        if (idx == null) return new ArrayList<>();
        return parseSemicolonList(row.getCell(idx));   // ← now semicolon
    }



    // ─── Helper methods ────────────────────────────────────────────────────────

    private String normalizeImageKey(String name) {
        if (name == null) return "";
        return name.trim()
                .toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("_", "-")
                .replaceAll("-+", "-")
                .replaceFirst("\\.[^.]*$", "");
    }

    private Map<String, String> parseDynamicFields(Row row, Map<String, Integer> colIndex) {
        Map<String, String> fields = new LinkedHashMap<>();
        Integer idx = colIndex.get("dynamic fields");
        if (idx == null) return fields;

        String raw = getCellValue(row.getCell(idx)).trim();
        if (raw.isEmpty()) return fields;

        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;

            int colon = trimmed.indexOf(':');
            if (colon > 0) {
                String key = trimmed.substring(0, colon).trim();
                String value = trimmed.substring(colon + 1).trim();
                if (!key.isEmpty()) {
                    fields.put(key, value);
                }
            } else if (!trimmed.isEmpty()) {
                fields.put(trimmed, "");
            }
        }
        return fields;
    }

    private Map<String, String> buildOptionalSizeMap(List<String> sizes, List<String> values) {
        Map<String, String> map = new LinkedHashMap<>();
        if (sizes.isEmpty()) return map;

        if (values.isEmpty() || (values.size() == 1 && values.get(0).isEmpty())) {
            sizes.forEach(s -> map.put(s, ""));
            return map;
        }

        if (values.size() == 1 && sizes.size() > 1) {
            String single = values.get(0);
            values = Collections.nCopies(sizes.size(), single);
        }

        int limit = Math.min(sizes.size(), values.size());
        for (int i = 0; i < limit; i++) {
            map.put(sizes.get(i), values.get(i));
        }
        return map;
    }

    private boolean getOptionalBoolean(Row row, Map<String, Integer> colIndex, String colName, boolean defaultValue) {
        Integer idx = colIndex.get(colName);
        if (idx == null) return defaultValue;
        Boolean val = getBooleanCellValue(row.getCell(idx));
        return val != null ? val : defaultValue;
    }

    private String getOptionalString(Row row, Map<String, Integer> colIndex, String colName, String defaultValue) {
        Integer idx = colIndex.get(colName);
        if (idx == null) return defaultValue;
        String val = getCellValue(row.getCell(idx));
        return val.isEmpty() ? defaultValue : val;
    }


    private <T> void autoExpand(List<String> sizes, List<T> values, String field, String product) {
        if (sizes == null || sizes.isEmpty()) return;
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Missing " + field + " for product: " + product);
        }
        if (values.size() == 1 && sizes.size() > 1) {
            T single = values.get(0);
            values.clear();
            values.addAll(Collections.nCopies(sizes.size(), single));
        }
        if (values.size() != sizes.size()) {
            throw new IllegalArgumentException(field + " count mismatch for " + product + " (sizes: " + sizes.size() + ", values: " + values.size() + ")");
        }
    }


    private List<String> parseCommaList(Cell cell) {
        String value = getCellValue(cell);
        if (value == null || value.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Boolean getBooleanCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case STRING:
                    String s = cell.getStringCellValue().trim().toLowerCase();
                    if (s.isEmpty()) return null;
                    return "true".equals(s) || "yes".equals(s) || "1".equals(s);
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case NUMERIC:
                    return cell.getNumericCellValue() == 1.0;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        try {
            switch (cell.getCellType()) {
                case STRING: return cell.getStringCellValue().trim();
                case NUMERIC: return String.valueOf(cell.getNumericCellValue());
                case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
                case FORMULA: return cell.getCellFormula();
                default: return "";
            }
        } catch (Exception e) {
            return "";
        }
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

    private List<String> buildCategoryPath(String subCategory) {
        if (subCategory == null || subCategory.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(subCategory.split(">"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }



    private ProductResponseDto mapToResponseDto(ProductEntity entity) {
        ProductResponseDto responseDto = new ProductResponseDto();

        // ─── Basic & list fields ───
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

        // ─── Inventory / Batch / Variant aggregation ───
        Map<String, Integer> stockBySize = new HashMap<>();
        int totalQuantity = 0;

        // Representative single values (fallback / summary)
        String selectedMfgDate   = null;
        String selectedExpDate   = null;
        String selectedBatchNo   = null;

        // Track earliest expiry
        LocalDate soonestExpiry = null;

        // NEW: Per-size maps for detailed view
        Map<String, String> sizeToMfgDate = new LinkedHashMap<>();
        Map<String, String> sizeToExpDate = new LinkedHashMap<>();

        List<InventoryEntity> batches = entity.getInventoryBatches();

        if (batches != null && !batches.isEmpty()) {
            for (InventoryEntity batch : batches) {
                // Batch number: last non-empty (most recent)
                if (batch.getBatchNo() != null && !batch.getBatchNo().trim().isEmpty()) {
                    selectedBatchNo = batch.getBatchNo().trim();
                }

                List<BatchVariant> variants = batch.getVariants();
                if (variants != null && !variants.isEmpty()) {
                    for (BatchVariant variant : variants) {
                        String sizeKey = (variant.getSize() != null && !variant.getSize().trim().isEmpty())
                                ? variant.getSize().trim()
                                : "DEFAULT";

                        int qty = (variant.getQuantity() != null) ? variant.getQuantity() : 0;
                        stockBySize.merge(sizeKey, qty, Integer::sum);
                        totalQuantity += qty;

                        // MFG date – collect per size + pick representative
                        String variantMfg = variant.getMfgDate();
                        if (variantMfg != null && !variantMfg.trim().isEmpty()) {
                            sizeToMfgDate.put(sizeKey, variantMfg.trim());

                            // Representative: take first valid one
                            if (selectedMfgDate == null) {
                                selectedMfgDate = variantMfg.trim();
                            }
                        }

                        // EXP date – collect per size + pick earliest
                        String variantExp = variant.getExpDate();
                        if (variantExp != null && !variantExp.trim().isEmpty()) {
                            sizeToExpDate.put(sizeKey, variantExp.trim());

                            try {
                                LocalDate exp = LocalDate.parse(variantExp.trim());
                                if (soonestExpiry == null || exp.isBefore(soonestExpiry)) {
                                    soonestExpiry = exp;
                                    selectedExpDate = variantExp.trim();
                                }
                            } catch (DateTimeParseException e) {
                                logger.warn("Invalid expiry date format → product {}, batch {}, value: {}",
                                        entity.getProductId(), batch.getBatchNo(), variantExp);
                                if (selectedExpDate == null) {
                                    selectedExpDate = variantExp.trim();
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fallback: if no batch data found, use per-size maps (first entry)
        if (selectedMfgDate == null && !sizeToMfgDate.isEmpty()) {
            selectedMfgDate = sizeToMfgDate.values().stream()
                    .filter(s -> !s.isEmpty())
                    .findFirst().orElse(null);
        }
        if (selectedExpDate == null && !sizeToExpDate.isEmpty()) {
            selectedExpDate = sizeToExpDate.values().stream()
                    .filter(s -> !s.isEmpty())
                    .findFirst().orElse(null);
        }

        // Set values
        responseDto.setProductQuantity(totalQuantity);
        responseDto.setStockBySize(stockBySize);

        // Keep existing scalar fields (fallback / summary)
        responseDto.setMfgDate(selectedMfgDate);
        responseDto.setExpDate(selectedExpDate);
        responseDto.setBatchNo(selectedBatchNo);

        // NEW: Add per-size detailed maps
        responseDto.setMfgDates(sizeToMfgDate);
        responseDto.setExpDates(sizeToExpDate);

        // ─── Images & dynamic fields ───
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