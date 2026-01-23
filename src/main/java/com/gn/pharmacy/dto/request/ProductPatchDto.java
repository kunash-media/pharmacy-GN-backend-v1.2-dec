package com.gn.pharmacy.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * DTO for **partial updates** (PATCH) of Products.
 * All fields are optional — null means "do not update this field".
 * Uses wrapper types (Boolean, Integer, Double) to distinguish "not set" from explicit false/0.
 */
public class ProductPatchDto {

    private String sku;
    private String productName;
    private String productCategory;
    private String productSubCategory;

    private List<BigDecimal> productPrice;
    private List<BigDecimal> productOldPrice;

    private String productStock;
    private String productStatus;
    private String productDescription;

    private Integer productQuantity;
    private Boolean prescriptionRequired;   // null = don't change, Boolean allows explicit false
    private String brandName;

    private String mfgDate;
    private String expDate;
    private String batchNo;

    private Double rating;

    private List<String> benefitsList;
    private List<String> ingredientsList;
    private List<String> directionsList;
    private List<String> categoryPath;

    private Map<String, String> productDynamicFields;

    private List<String> productSizes;

    private Boolean approved;   // null = don't change
    private Boolean deleted;    // null = don't change

    // ────────────────────────────────────────────────
    // Constructors
    // ────────────────────────────────────────────────

    public ProductPatchDto() {
        // Default no-arg constructor
    }

    // ────────────────────────────────────────────────
    // Getters & Setters (with safe defaults for collections/maps)
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
        this.productPrice = productPrice;
    }

    public List<BigDecimal> getProductOldPrice() {
        return productOldPrice;
    }

    public void setProductOldPrice(List<BigDecimal> productOldPrice) {
        this.productOldPrice = productOldPrice;
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

    public Boolean getPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(Boolean prescriptionRequired) {
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
        this.benefitsList = benefitsList;
    }

    public List<String> getIngredientsList() {
        return ingredientsList;
    }

    public void setIngredientsList(List<String> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    public List<String> getDirectionsList() {
        return directionsList;
    }

    public void setDirectionsList(List<String> directionsList) {
        this.directionsList = directionsList;
    }

    public List<String> getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(List<String> categoryPath) {
        this.categoryPath = categoryPath;
    }

    public Map<String, String> getProductDynamicFields() {
        return productDynamicFields;
    }

    public void setProductDynamicFields(Map<String, String> productDynamicFields) {
        this.productDynamicFields = productDynamicFields;
    }

    public List<String> getProductSizes() {
        return productSizes;
    }

    public void setProductSizes(List<String> productSizes) {
        this.productSizes = productSizes;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}