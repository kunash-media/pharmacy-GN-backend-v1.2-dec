package com.gn.pharmacy.service.serviceImpl;
import com.gn.pharmacy.dto.request.ProductRequestDto;
import com.gn.pharmacy.dto.response.BulkUploadResponse;
import com.gn.pharmacy.dto.response.ProductResponseDto;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponseDto createProduct(ProductRequestDto requestDto) throws Exception {
        log.debug("Creating new product with name: {}", requestDto.getProductName());

        ProductEntity entity = new ProductEntity();

        entity.setProductName(requestDto.getProductName());
        entity.setProductCategory(requestDto.getProductCategory());
        entity.setProductSubCategory(requestDto.getProductSubCategory());

        // NEW: Auto-build categoryPath
        List<String> path = requestDto.getCategoryPath() != null
                ? new ArrayList<>(requestDto.getCategoryPath())
                : buildCategoryPath(requestDto.getProductSubCategory());
        entity.setCategoryPath(path);

        entity.setProductPrice(requestDto.getProductPrice());
        entity.setProductOldPrice(requestDto.getProductOldPrice());
        entity.setProductStock(requestDto.getProductStock());
        entity.setProductStatus(requestDto.getProductStatus());
        entity.setProductDescription(requestDto.getProductDescription());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setProductQuantity(requestDto.getProductQuantity());

        // NEW: Set new fields
        entity.setPrescriptionRequired(requestDto.isPrescriptionRequired());
        entity.setBrandName(requestDto.getBrandName());
        entity.setMfgDate(requestDto.getMfgDate());
        entity.setExpDate(requestDto.getExpDate());
        entity.setBatchNo(requestDto.getBatchNo());
        entity.setBenefitsList(requestDto.getBenefitsList());
        entity.setIngredientsList(requestDto.getIngredientsList());


        entity.setProductMainImage(requestDto.getProductMainImage().getBytes());

        List<byte[]> subImages = requestDto.getProductSubImages() != null
                ? requestDto.getProductSubImages().stream().map(file -> {
            try {
                return file.getBytes();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList())
                : new ArrayList<>();

        entity.setProductSubImages(subImages);

        //entity.setProductDynamicFields(requestDto.getProductDynamicFields());

        // In createProduct method (around line with setProductDynamicFields)
        if (requestDto.getProductDynamicFields() != null) {
            entity.setProductDynamicFields(requestDto.getProductDynamicFields());
        } else {
            entity.setProductDynamicFields(new HashMap<>());  // Optional: Set empty if null
        }

        if (requestDto.getProductSizes() != null) {
            entity.setProductSizes(requestDto.getProductSizes());
        }

        ProductEntity savedEntity = productRepository.save(entity);
        log.debug("Product saved with ID: {}", savedEntity.getProductId());
        return mapToResponseDto(savedEntity);
    }

    @Override
    public ProductResponseDto getProduct(Long productId) {
        log.debug("Fetching product by ID: {}", productId);
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", productId);
                    return new RuntimeException("Product not found");
                });
        return mapToResponseDto(entity);
    }

    @Override
    public Page<ProductResponseDto> getAllProducts(int page, int size) {
        log.debug("Fetching all products - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> productPage = productRepository.findAll(pageable);
        log.debug("Found {} products on page {}", productPage.getNumberOfElements(), page);
        return productPage.map(this::mapToResponseDto);
    }

    @Override
    public List<ProductResponseDto> getProductsByCategory(String productCategory) {
        log.debug("Fetching products by category: {}", productCategory);
        List<ProductEntity> products = productRepository.findByProductCategory(productCategory);
        log.debug("Found {} products for category: {}", products.size(), productCategory);
        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> getProductsBySubCategory(String subCategory) {
        log.debug("Fetching products by sub category: {}", subCategory);
        List<ProductEntity> products = productRepository.findByProductSubCategory(subCategory);
        log.debug("Found {} products for sub category: {}", products.size(), subCategory);
        return products.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    // HELPER: Build categoryPath from subCategory (e.g., "Chronic Care > Diabetes" → ["Chronic Care", "Diabetes"])
    private List<String> buildCategoryPath(String subCategory) {
        if (subCategory == null || subCategory.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(subCategory.split(">"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    //=====================================================================
    //    NEW-PATCH      getProductsByCategoryPath                        //
    //=====================================================================
    @Override
    public List<ProductResponseDto> getProductsByCategoryPath(List<String> path) {
        if (path == null || path.isEmpty()) return new ArrayList<>();
        List<ProductEntity> products = productRepository.findByCategoryPath(path, path.size());
        return products.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    //=====================================================================
    //    NEW-PATCH     getProductsBySubPath                        //
    //=====================================================================
    @Override
    public List<ProductResponseDto> getProductsBySubPath(String subPath) {
        if (subPath == null || subPath.trim().isEmpty()) return new ArrayList<>();
        List<ProductEntity> products = productRepository.findByCategoryPathContaining(subPath.trim());
        return products.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    public ProductResponseDto updateProduct(Long id, ProductRequestDto requestDto) throws Exception {
        log.debug("Updating product with ID: {}", id);

        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", id);
                    return new RuntimeException("Product not found");
                });
        entity.setProductName(requestDto.getProductName());
        entity.setProductCategory(requestDto.getProductCategory());
        entity.setProductSubCategory(requestDto.getProductSubCategory());
        entity.setProductPrice(requestDto.getProductPrice());
        entity.setProductOldPrice(requestDto.getProductOldPrice());
        entity.setProductStock(requestDto.getProductStock());
        entity.setProductStatus(requestDto.getProductStatus());
        entity.setProductDescription(requestDto.getProductDescription());
        entity.setProductQuantity(requestDto.getProductQuantity());

        // NEW: Set new fields
        entity.setPrescriptionRequired(requestDto.isPrescriptionRequired());
        entity.setBrandName(requestDto.getBrandName());
        entity.setMfgDate(requestDto.getMfgDate());
        entity.setExpDate(requestDto.getExpDate());
        entity.setBatchNo(requestDto.getBatchNo());
        entity.setBenefitsList(requestDto.getBenefitsList());
        entity.setIngredientsList(requestDto.getIngredientsList());

        entity.setProductMainImage(requestDto.getProductMainImage().getBytes());

        List<byte[]> subImages = requestDto.getProductSubImages() != null
                ? requestDto.getProductSubImages().stream().map(file -> {
            try {
                return file.getBytes();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList())
                : new ArrayList<>();
        entity.setProductSubImages(subImages);
        //entity.setProductDynamicFields(requestDto.getProductDynamicFields());

        if (requestDto.getProductDynamicFields() != null) {
            entity.setProductDynamicFields(requestDto.getProductDynamicFields());
        } else {
            entity.setProductDynamicFields(new HashMap<>());  // Optional: Set empty if null
        }

        if (requestDto.getProductSizes() != null) {
            entity.setProductSizes(requestDto.getProductSizes());
        }

        if (requestDto.getProductCategory() != null) entity.setProductCategory(requestDto.getProductCategory());
        if (requestDto.getProductSubCategory() != null) {
            entity.setProductSubCategory(requestDto.getProductSubCategory());
            // Rebuild path
            entity.setCategoryPath(buildCategoryPath(requestDto.getProductSubCategory()));
        }
        if (requestDto.getCategoryPath() != null) {
            entity.setCategoryPath(new ArrayList<>(requestDto.getCategoryPath()));
        }

        ProductEntity updatedEntity = productRepository.save(entity);
        log.debug("Product updated successfully with ID: {}", id);
        return mapToResponseDto(updatedEntity);
    }

    @Override
    public ProductResponseDto patchProduct(Long id, ProductRequestDto requestDto) throws Exception {
        log.debug("Patching product with ID: {}", id);

        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", id);
                    return new RuntimeException("Product not found");
                });

        if (requestDto.getProductName() != null) entity.setProductName(requestDto.getProductName());
        if (requestDto.getProductCategory() != null) entity.setProductCategory(requestDto.getProductCategory());

        if (requestDto.getProductSubCategory() != null) {
            entity.setProductSubCategory(requestDto.getProductSubCategory());
            // Rebuild path
            entity.setCategoryPath(buildCategoryPath(requestDto.getProductSubCategory()));
        }
        if (requestDto.getCategoryPath() != null) {
            entity.setCategoryPath(new ArrayList<>(requestDto.getCategoryPath()));
        }


        if (requestDto.getProductPrice() != null) entity.setProductPrice(requestDto.getProductPrice());
        if (requestDto.getProductOldPrice() != null) entity.setProductOldPrice(requestDto.getProductOldPrice());
        if (requestDto.getProductStock() != null) entity.setProductStock(requestDto.getProductStock());
        if (requestDto.getProductStatus() != null) entity.setProductStatus(requestDto.getProductStatus());
        if (requestDto.getProductQuantity() != null) entity.setProductQuantity(requestDto.getProductQuantity());
        if (requestDto.getProductDescription() != null) entity.setProductDescription(requestDto.getProductDescription());

        // NEW: Patch new fields if provided
        if (requestDto.isPrescriptionRequired()) entity.setPrescriptionRequired(requestDto.isPrescriptionRequired());
        if (requestDto.getBrandName() != null) entity.setBrandName(requestDto.getBrandName());
        if (requestDto.getMfgDate() != null) entity.setMfgDate(requestDto.getMfgDate());
        if (requestDto.getExpDate() != null) entity.setExpDate(requestDto.getExpDate());
        if (requestDto.getBatchNo() != null) entity.setBatchNo(requestDto.getBatchNo());
        if (requestDto.getBenefitsList() != null && !requestDto.getBenefitsList().isEmpty()) entity.setBenefitsList(requestDto.getBenefitsList());
        if (requestDto.getIngredientsList() != null && !requestDto.getIngredientsList().isEmpty()) entity.setIngredientsList(requestDto.getIngredientsList());

        if (requestDto.getProductMainImage() != null) entity.setProductMainImage(requestDto.getProductMainImage().getBytes());
        if (requestDto.getProductSubImages() != null) {
            List<byte[]> subImages = requestDto.getProductSubImages().stream().map(file -> {
                try {
                    return file.getBytes();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            entity.setProductSubImages(subImages);
        }
        if (requestDto.getProductDynamicFields() != null) entity.setProductDynamicFields(requestDto.getProductDynamicFields());

        if (requestDto.getProductSizes() != null) {
            entity.setProductSizes(requestDto.getProductSizes());
        }

        ProductEntity updatedEntity = productRepository.save(entity);
        log.debug("Product patched successfully with ID: {}", id);
        return mapToResponseDto(updatedEntity);
    }

    @Override
    public void deleteProduct(Long productId) {
        log.debug("Deleting product with ID: {}", productId);
        productRepository.deleteById(productId);
        log.debug("Product deleted successfully with ID: {}", productId);
    }

    private ProductResponseDto mapToResponseDto(ProductEntity entity) {
        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setProductId(entity.getProductId());
        responseDto.setProductName(entity.getProductName());
        responseDto.setProductCategory(entity.getProductCategory());
        responseDto.setProductSubCategory(entity.getProductSubCategory());
        responseDto.setCategoryPath(new ArrayList<>(entity.getCategoryPath())); // NEW
        responseDto.setProductPrice(entity.getProductPrice());
        responseDto.setProductOldPrice(entity.getProductOldPrice());
        responseDto.setProductStock(entity.getProductStock());
        responseDto.setProductStatus(entity.getProductStatus());
        responseDto.setProductDescription(entity.getProductDescription());
        responseDto.setCreatedAt(entity.getCreatedAt());
        responseDto.setProductQuantity(entity.getProductQuantity());
        responseDto.setProductMainImage("/api/products/" + entity.getProductId() + "/image");
        responseDto.setProductSubImages(IntStream.range(0, entity.getProductSubImages().size())
                .mapToObj(i -> "/api/products/" + entity.getProductId() + "/subimage/" + i)
                .collect(Collectors.toList()));
        responseDto.setProductDynamicFields(entity.getProductDynamicFields());

        responseDto.setProductSizes(entity.getProductSizes());

        // NEW: Map new fields
        responseDto.setPrescriptionRequired(entity.isPrescriptionRequired());
        responseDto.setBrandName(entity.getBrandName());
        responseDto.setMfgDate(entity.getMfgDate());
        responseDto.setExpDate(entity.getExpDate());
        responseDto.setBatchNo(entity.getBatchNo());
        responseDto.setBenefitsList(entity.getBenefitsList());
        responseDto.setIngredientsList(entity.getIngredientsList());

        return responseDto;
    }

    //=================== bulk product handling api ======================//

    @Override
    public BulkUploadResponse bulkCreateProducts(MultipartFile excelFile, List<MultipartFile> images) throws Exception {
        log.debug("Starting bulk product creation from Excel");

        // Image mapping with extension stripping
        Map<String, MultipartFile> imageMap = new HashMap<>();
        if (images != null) {
            for (MultipartFile image : images) {
                String fullFilename = image.getOriginalFilename();
                if (fullFilename != null) {
                    // Strip extension for key (e.g., "image.jpg" -> "image")
                    String baseName = fullFilename.contains(".") ? fullFilename.substring(0, fullFilename.lastIndexOf('.')) : fullFilename;
                    imageMap.put(baseName.trim().toLowerCase(), image);  // Trim & lowercase for robustness
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

            // Skip header row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String productName = getCellValue(row.getCell(0));

                // Check for duplicate by name (add null/empty check)
                if (productName == null || productName.trim().isEmpty()) {
                    skippedCount++;
                    skippedReasons.add("Empty or missing product name in row " + (row.getRowNum() + 1));
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

                // Safe BigDecimal parsing for price (required)
                String priceStr = getCellValue(row.getCell(3));
                if (priceStr == null || priceStr.trim().isEmpty()) {
                    skippedCount++;
                    skippedReasons.add("Missing or empty price for product: " + productName);
                    continue;
                }
                try {
                    dto.setProductPrice(new BigDecimal(priceStr));
                } catch (NumberFormatException e) {
                    skippedCount++;
                    skippedReasons.add("Invalid price for product: " + productName + " (" + priceStr + ")");
                    continue;
                }

                // Safe BigDecimal parsing for old price (optional)
                String oldPriceStr = getCellValue(row.getCell(4));
                if (!oldPriceStr.trim().isEmpty()) {
                    try {
                        dto.setProductOldPrice(new BigDecimal(oldPriceStr));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid old price for product {}: {}", productName, oldPriceStr);
                        // Don't skip product for optional field
                    }
                }

                // Stock as String (required)
                String stock = getCellValue(row.getCell(5));
                if (stock == null || stock.trim().isEmpty()) {
                    skippedCount++;
                    skippedReasons.add("Invalid or missing stock for product: " + productName);
                    continue;
                }
                dto.setProductStock(stock);

                dto.setProductStatus(getCellValue(row.getCell(6)));
                dto.setProductDescription(getCellValue(row.getCell(7)));

                // Safe integer parsing for quantity (required)
                Integer quantity = getIntegerCellValue(row.getCell(8));
                if (quantity == null) {
                    skippedCount++;
                    skippedReasons.add("Invalid or missing quantity for product: " + productName);
                    continue;
                }
                dto.setProductQuantity(quantity);

                // NEW: Parse prescriptionRequired (Column 13: "true"/"false" or "yes"/"no")
                Boolean prescriptionReq = getBooleanCellValue(row.getCell(13));
                if (prescriptionReq != null) {
                    dto.setPrescriptionRequired(prescriptionReq);
                } else {
                    log.warn("No prescription required flag for product: {}", productName);
                    dto.setPrescriptionRequired(false);  // Default
                }

                // NEW: Brand Name (Column 14, optional String)
                String brandName = getCellValue(row.getCell(14));
                dto.setBrandName(brandName);

                // NEW: MFG Date (Column 15, optional String)
                String mfgDate = getCellValue(row.getCell(15));
                dto.setMfgDate(mfgDate);

                // NEW: Exp Date (Column 16, optional String)
                String expDate = getCellValue(row.getCell(16));
                dto.setExpDate(expDate);

                // NEW: Batch No (Column 17, optional String)
                String batchNo = getCellValue(row.getCell(17));
                dto.setBatchNo(batchNo);

                // Main image lookup with extension stripping
                String mainImageFilename = getCellValue(row.getCell(9));
                if (mainImageFilename != null && !mainImageFilename.trim().isEmpty()) {
                    // Strip extension from Excel value (e.g., "image.jpg" -> "image")
                    String mainBaseName = mainImageFilename.contains(".") ?
                            mainImageFilename.substring(0, mainImageFilename.lastIndexOf('.')) : mainImageFilename;
                    MultipartFile mainImage = imageMap.get(mainBaseName.trim().toLowerCase());
                    if (mainImage != null) {
                        dto.setProductMainImage(mainImage);
                    } else {
                        skippedCount++;
                        skippedReasons.add("Missing main image for product: " + productName + " (" + mainImageFilename + ")");
                        continue;
                    }
                } else {
                    log.warn("No main image specified for product: {}", productName);
                    // If main image is required, add: continue; here to skip
                }

                // Sub images lookup with extension stripping
                String subImagesStr = getCellValue(row.getCell(10));
                List<MultipartFile> subImageFiles = new ArrayList<>();
                if (subImagesStr != null && !subImagesStr.trim().isEmpty()) {
                    String[] subFilenames = subImagesStr.split(",");
                    boolean hasMissingSub = false;
                    for (String subFilename : subFilenames) {
                        String trimmedSub = subFilename.trim();
                        if (trimmedSub.isEmpty()) continue;

                        // Strip extension from each sub-filename
                        String subBaseName = trimmedSub.contains(".") ?
                                trimmedSub.substring(0, trimmedSub.lastIndexOf('.')) : trimmedSub;
                        MultipartFile subImage = imageMap.get(subBaseName.toLowerCase());
                        if (subImage != null) {
                            subImageFiles.add(subImage);
                        } else {
                            hasMissingSub = true;
                            skippedReasons.add("Missing sub image for product: " + productName + " (" + trimmedSub + ")");
                        }
                    }
                    if (hasMissingSub) {
                        skippedCount++;
                        continue;
                    }
                }
                dto.setProductSubImages(subImageFiles);

                // Dynamic fields (format: key1:value1,key2:value2)
                String dynamicFieldsStr = getCellValue(row.getCell(11));
                Map<String, String> dynamicFields = new HashMap<>();
                if (dynamicFieldsStr != null && !dynamicFieldsStr.trim().isEmpty()) {
                    String[] pairs = dynamicFieldsStr.split(",");
                    for (String pair : pairs) {
                        String trimmedPair = pair.trim();
                        if (trimmedPair.isEmpty()) continue;
                        String[] kv = trimmedPair.split(":");
                        if (kv.length == 2) {
                            dynamicFields.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
                dto.setProductDynamicFields(dynamicFields);

                // Sizes (comma-separated)
                String sizesStr = getCellValue(row.getCell(12));
                List<String> sizes = new ArrayList<>();
                if (sizesStr != null && !sizesStr.trim().isEmpty()) {
                    sizes = Arrays.stream(sizesStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
                dto.setProductSizes(sizes);

                // NEW: Benefits List (Column 18, comma-separated, optional)
                String benefitsStr = getCellValue(row.getCell(18));
                List<String> benefits = new ArrayList<>();
                if (benefitsStr != null && !benefitsStr.trim().isEmpty()) {
                    benefits = Arrays.stream(benefitsStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
                dto.setBenefitsList(benefits);

                // NEW: Directions List (Column 19, comma-separated, optional)
                String directionsStr = getCellValue(row.getCell(19));
                List<String> directions = new ArrayList<>();
                if (directionsStr != null && !directionsStr.trim().isEmpty()) {
                    directions = Arrays.stream(directionsStr.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                }
                dto.setIngredientsList(directions);

                // Create the product using existing createProduct method
                try {
                    createProduct(dto);
                    uploadedCount++;
                    log.debug("Successfully uploaded product: {}", productName);
                } catch (Exception e) {
                    skippedCount++;
                    skippedReasons.add("Error creating product: " + productName + " - " + e.getMessage());
                    log.error("Failed to create product {}: {}", productName, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
        }

        BulkUploadResponse response = new BulkUploadResponse();
        response.setUploadedCount(uploadedCount);
        response.setSkippedCount(skippedCount);
        response.setSkippedReasons(skippedReasons);

        log.debug("Bulk creation completed: {} uploaded, {} skipped", uploadedCount, skippedCount);
        return response;
    }

    // Helper method: Safe integer parsing (handles Excel numeric as double)
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
                    if (numVal == Math.floor(numVal)) {  // Check if whole number
                        return (int) numVal;
                    } else {
                        log.warn("Non-integer numeric value found: {}", numVal);
                        return null;  // Or (int) Math.floor(numVal) if you want to truncate
                    }
                case BOOLEAN:
                    return cell.getBooleanCellValue() ? 1 : 0;
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer from cell: {}", cell);
            return null;
        }
    }

    // NEW HELPER: For boolean parsing (handles "true"/"false", "yes"/"no", 1/0)
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
            log.warn("Failed to parse boolean from cell: {}", cell);
            return null;
        }
    }

    // Helper method: For strings and decimals (trimmed)
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

}