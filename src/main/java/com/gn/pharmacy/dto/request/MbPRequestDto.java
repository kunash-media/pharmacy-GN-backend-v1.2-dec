package com.gn.pharmacy.dto.request;

import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

public class MbPRequestDto {
    private String sku;
    private String title;
    private String category;
    private String subCategory;

    private List<Double> price = new ArrayList<>();
    private List<Double> originalPrice = new ArrayList<>();

//    private Double price;
//    private Double originalPrice;
    private Integer discount;
    private Double rating;
    private Integer reviewCount;
    private String brand;
    private Boolean inStock;
    private Integer stockQuantity;
    private List<String> description = new ArrayList<>();
    private List<String> sizes = new ArrayList<>();
    private List<String> features = new ArrayList<>();
    private MultipartFile mainImage;
    private List<MultipartFile> subImages = new ArrayList<>();
    private String specifications;

    // Getters and Setters
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

    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public MultipartFile getMainImage() { return mainImage; }
    public void setMainImage(MultipartFile mainImage) { this.mainImage = mainImage; }

    public List<MultipartFile> getSubImages() { return subImages; }
    public void setSubImages(List<MultipartFile> subImages) { this.subImages = subImages; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
}