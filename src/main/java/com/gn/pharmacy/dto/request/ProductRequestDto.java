package com.gn.pharmacy.dto.request;

import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for creating/updating Products.
 * Supports per-size variants with quantity, manufacturing date, and expiry date for initial inventory batch.
 */
public class ProductRequestDto {

    private String sku;
    private String productName;
    private String productCategory;
    private String productSubCategory;

    private List<BigDecimal> productPrice = new ArrayList<>();
    private List<BigDecimal> productOldPrice = new ArrayList<>();

    private String productStock = "In Stock";           // e.g., "In Stock", "Low Stock" — status string
    private String productStatus;          // e.g., "ACTIVE", "INACTIVE"
    private String productDescription;

    private Integer productQuantity;       // Fallback total quantity (used if no sizeQuantities)
    private boolean prescriptionRequired;
    private String brandName;

    // Fallback dates/batch (used when no per-size dates provided)
    private String mfgDate;                // nullable
    private String expDate;                // nullable
    private String batchNo;                // optional batch number

    private Double rating;

    private List<String> benefitsList = new ArrayList<>();
    private List<String> ingredientsList = new ArrayList<>();
    private List<String> directionsList = new ArrayList<>();
    private List<String> categoryPath = new ArrayList<>();

    // Images
    private MultipartFile productMainImage;
    private List<MultipartFile> productSubImages = new ArrayList<>();

    private Map<String, String> productDynamicFields = new HashMap<>();
    private List<String> productSizes = new ArrayList<>();

    // NEW: Per-size data for initial inventory batch creation (preferred over single quantity)
    private Map<String, Integer> sizeQuantities = new HashMap<>();      // size → quantity (required if sizes exist)
    private Map<String, String> sizeMfgDates = new HashMap<>();        // size → mfg date (nullable)
    private Map<String, String> sizeExpDates = new HashMap<>();        // size → expiry date (nullable)

    private boolean approved;
    private boolean deleted;

    // ────────────────────────────────────────────────
    // Getters & Setters
    // ────────────────────────────────────────────────

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductSubCategory() {
        return productSubCategory;
    }

    public void setProductSubCategory(String productSubCategory) {
        this.productSubCategory = productSubCategory;
    }

    public List<BigDecimal> getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(List<BigDecimal> productPrice) {
        this.productPrice = productPrice != null ? productPrice : new ArrayList<>();
    }

    public List<BigDecimal> getProductOldPrice() {
        return productOldPrice;
    }

    public void setProductOldPrice(List<BigDecimal> productOldPrice) {
        this.productOldPrice = productOldPrice != null ? productOldPrice : new ArrayList<>();
    }


    public String getProductStock() {
        return productStock;
    }

    public void setProductStock(String productStock) {
        this.productStock = productStock;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(String mfgDate) {
        this.mfgDate = mfgDate;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<String> getBenefitsList() {
        return benefitsList;
    }

    public void setBenefitsList(List<String> benefitsList) {
        this.benefitsList = benefitsList != null ? benefitsList : new ArrayList<>();
    }

    public List<String> getIngredientsList() {
        return ingredientsList;
    }

    public void setIngredientsList(List<String> ingredientsList) {
        this.ingredientsList = ingredientsList != null ? ingredientsList : new ArrayList<>();
    }

    public List<String> getDirectionsList() {
        return directionsList;
    }

    public void setDirectionsList(List<String> directionsList) {
        this.directionsList = directionsList != null ? directionsList : new ArrayList<>();
    }

    public List<String> getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(List<String> categoryPath) {
        this.categoryPath = categoryPath != null ? categoryPath : new ArrayList<>();
    }

    public MultipartFile getProductMainImage() {
        return productMainImage;
    }

    public void setProductMainImage(MultipartFile productMainImage) {
        this.productMainImage = productMainImage;
    }

    public List<MultipartFile> getProductSubImages() {
        return productSubImages;
    }

    public void setProductSubImages(List<MultipartFile> productSubImages) {
        this.productSubImages = productSubImages != null ? productSubImages : new ArrayList<>();
    }

    public Map<String, String> getProductDynamicFields() {
        return productDynamicFields;
    }

    public void setProductDynamicFields(Map<String, String> productDynamicFields) {
        this.productDynamicFields = productDynamicFields != null ? productDynamicFields : new HashMap<>();
    }

    public List<String> getProductSizes() {
        return productSizes;
    }

    public void setProductSizes(List<String> productSizes) {
        this.productSizes = productSizes != null ? productSizes : new ArrayList<>();
    }

    public Map<String, Integer> getSizeQuantities() {
        return sizeQuantities;
    }

    public void setSizeQuantities(Map<String, Integer> sizeQuantities) {
        this.sizeQuantities = sizeQuantities != null ? sizeQuantities : new HashMap<>();
    }

    public Map<String, String> getSizeMfgDates() {
        return sizeMfgDates;
    }

    public void setSizeMfgDates(Map<String, String> sizeMfgDates) {
        this.sizeMfgDates = sizeMfgDates != null ? sizeMfgDates : new HashMap<>();
    }

    public Map<String, String> getSizeExpDates() {
        return sizeExpDates;
    }

    public void setSizeExpDates(Map<String, String> sizeExpDates) {
        this.sizeExpDates = sizeExpDates != null ? sizeExpDates : new HashMap<>();
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}