package com.gn.pharmacy.dto.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MbPResponseDto {
    private Long id;
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
    private Integer stockQuantity;
    private List<String> description = new ArrayList<>();
    private List<String> productSizes = new ArrayList<>();
    private List<String> features = new ArrayList<>();
    private String specifications;
    private Date createdAt;

    private String mainImageUrl;
    private List<String> subImageUrls = new ArrayList<>();

    private Boolean deleted;

    private Boolean approved;


    private Map<String, Integer> stockBySize;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }


    public List<Double> getPrice() {
        return price;
    }

    public void setPrice(List<Double> price) {
        this.price = price;
    }

    public List<Double> getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(List<Double> originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getDiscount() { return discount; }
    public void setDiscount(Integer discount) { this.discount = discount; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public List<String> getDescription() { return description; }
    public void setDescription(List<String> description) { this.description = description; }


    public List<String> getProductSizes() {
        return productSizes;
    }

    public void setProductSizes(List<String> productSizes) {
        this.productSizes = productSizes;
    }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getMainImageUrl() { return mainImageUrl; }
    public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }

    public List<String> getSubImageUrls() { return subImageUrls; }
    public void setSubImageUrls(List<String> subImageUrls) { this.subImageUrls = subImageUrls; }

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


    public Map<String, Integer> getStockBySize() {
        return stockBySize;
    }

    public void setStockBySize(Map<String, Integer> stockBySize) {
        this.stockBySize = stockBySize;
    }
}