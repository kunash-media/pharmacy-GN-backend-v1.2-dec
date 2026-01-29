package com.gn.pharmacy.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductResponseDto {
    private Long productId;
    private String sku;
    private String productName;
    private String productCategory;
    private String productSubCategory;

    private List<BigDecimal> productPrice = new ArrayList<>();
    private List<BigDecimal> productOldPrice = new ArrayList<>();

    private String productStock;
    private String productStatus;
    private String productDescription;
    private LocalDateTime createdAt;

    private Integer productQuantity;
    
    private boolean prescriptionRequired;
    private String brandName;
    private String mfgDate;
    private String expDate;
    private String batchNo;
    private Double rating;
    private List<String> benefitsList = new ArrayList<>();
    private List<String> ingredientsList = new ArrayList<>();
    private List<String> directionsList = new ArrayList<>();
    private List<String> categoryPath = new ArrayList<>();
    private String productMainImage;
    private List<String> productSubImages = new ArrayList<>();
    private Map<String, String> productDynamicFields;
    private List<String> productSizes = new ArrayList<>();


    private Map<String, Integer> stockBySize;

    private Map<String, String> mfgDates = new LinkedHashMap<>();   // size → mfg date
    private Map<String, String> expDates = new LinkedHashMap<>();   // size → exp date

    private boolean isApproved;
    private boolean isDeleted;

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getProductSubCategory() { return productSubCategory; }
    public void setProductSubCategory(String productSubCategory) { this.productSubCategory = productSubCategory; }


    public List<BigDecimal> getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(List<BigDecimal> productPrice) {
        this.productPrice = productPrice;
    }

    public List<BigDecimal> getProductOldPrice() {
        return productOldPrice;
    }

    public void setProductOldPrice(List<BigDecimal> productOldPrice) {
        this.productOldPrice = productOldPrice;
    }

    public String getProductStock() { return productStock; }
    public void setProductStock(String productStock) { this.productStock = productStock; }

    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getProductQuantity() { return productQuantity; }
    public void setProductQuantity(Integer productQuantity) { this.productQuantity = productQuantity; }

    public boolean isPrescriptionRequired() { return prescriptionRequired; }
    public void setPrescriptionRequired(boolean prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getMfgDate() { return mfgDate; }
    public void setMfgDate(String mfgDate) { this.mfgDate = mfgDate; }

    public String getExpDate() { return expDate; }
    public void setExpDate(String expDate) { this.expDate = expDate; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public List<String> getBenefitsList() { return benefitsList; }
    public void setBenefitsList(List<String> benefitsList) { this.benefitsList = benefitsList; }

    public List<String> getIngredientsList() { return ingredientsList; }
    public void setIngredientsList(List<String> ingredientsList) { this.ingredientsList = ingredientsList; }

    public List<String> getDirectionsList() { return directionsList; }
    public void setDirectionsList(List<String> directionsList) { this.directionsList = directionsList; }

    public List<String> getCategoryPath() { return categoryPath; }
    public void setCategoryPath(List<String> categoryPath) { this.categoryPath = categoryPath; }

    public String getProductMainImage() { return productMainImage; }
    public void setProductMainImage(String productMainImage) { this.productMainImage = productMainImage; }

    public List<String> getProductSubImages() { return productSubImages; }
    public void setProductSubImages(List<String> productSubImages) { this.productSubImages = productSubImages; }

    public Map<String, String> getProductDynamicFields() { return productDynamicFields; }
    public void setProductDynamicFields(Map<String, String> productDynamicFields) { this.productDynamicFields = productDynamicFields; }

    public List<String> getProductSizes() { return productSizes; }
    public void setProductSizes(List<String> productSizes) { this.productSizes = productSizes; }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Map<String, Integer> getStockBySize() {
        return stockBySize;
    }

    public void setStockBySize(Map<String, Integer> stockBySize) {
        this.stockBySize = stockBySize;
    }


    public Map<String, String> getMfgDates() {
        return mfgDates;
    }

    public void setMfgDates(Map<String, String> mfgDates) {
        this.mfgDates = mfgDates;
    }

    public Map<String, String> getExpDates() {
        return expDates;
    }

    public void setExpDates(Map<String, String> expDates) {
        this.expDates = expDates;
    }
}