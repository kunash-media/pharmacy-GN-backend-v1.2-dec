package com.gn.pharmacy.dto.request;

import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for creating/updating MB Products (marketplace/brand products).
 * Supports per-size variants with quantity, manufacturing date, and expiry date.
 */
public class MbPRequestDto {

    private String sku;
    private String title;
    private String category;
    private String subCategory;

    private List<Double> price = new ArrayList<>();
    private List<Double> originalPrice = new ArrayList<>();

    private Integer discount;
    private Double rating;
    private Integer reviewCount;
    private String brand;
    private Boolean inStock;

    private List<String> description = new ArrayList<>();
    private List<String> productSizes = new ArrayList<>();
    private List<String> features = new ArrayList<>();

    // Images
    private MultipartFile mainImage;
    private List<MultipartFile> subImages = new ArrayList<>();

    private String specifications;

    // NEW: Per-size data for initial inventory batch creation
    private Map<String, Integer> sizeQuantities = new HashMap<>();      // size -> quantity (required if sizes exist)
    private Map<String, String> sizeMfgDates = new HashMap<>();        // size -> mfg date (nullable)
    private Map<String, String> sizeExpDates = new HashMap<>();        // size -> expiry date (nullable)

    // Optional: If admin wants to specify batch number during creation
    private String batchNo;

    // Admin approval flag
    private Boolean approved;



    // ────────────────────────────────────────────────
    // Getters & Setters
    // ────────────────────────────────────────────────

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public List<Double> getPrice() {
        return price;
    }

    public void setPrice(List<Double> price) {
        this.price = price != null ? price : new ArrayList<>();
    }

    public List<Double> getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(List<Double> originalPrice) {
        this.originalPrice = originalPrice != null ? originalPrice : new ArrayList<>();
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description != null ? description : new ArrayList<>();
    }

    public List<String> getProductSizes() {
        return productSizes;
    }

    public void setProductSizes(List<String> productSizes) {
        this.productSizes = productSizes != null ? productSizes : new ArrayList<>();
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features != null ? features : new ArrayList<>();
    }

    public MultipartFile getMainImage() {
        return mainImage;
    }

    public void setMainImage(MultipartFile mainImage) {
        this.mainImage = mainImage;
    }

    public List<MultipartFile> getSubImages() {
        return subImages;
    }

    public void setSubImages(List<MultipartFile> subImages) {
        this.subImages = subImages != null ? subImages : new ArrayList<>();
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
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

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
}