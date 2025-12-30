package com.gn.pharmacy.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "mb_products")
@SQLDelete(sql = "UPDATE mb_products SET is_deleted = true WHERE id = ?")
public class MbPEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    private String title;
    private String category;

    @Column(name = "sub_category")
    private String subCategory;

    // CHANGED: Now List<Double> instead of Double
    @ElementCollection
    @CollectionTable(name = "mbp_prices", joinColumns = @JoinColumn(name = "mbp_id"))
    @Column(name = "price")
    private List<Double> price = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "mbp_original_prices", joinColumns = @JoinColumn(name = "mbp_id"))
    @Column(name = "original_price")
    private List<Double> originalPrice = new ArrayList<>();


    private Integer discount;
    private Double rating;
    private Integer reviewCount;
    private String brand;
    private Boolean inStock;
    private Integer stockQuantity;

    @ElementCollection
    @CollectionTable(name = "mbp_description", joinColumns = @JoinColumn(name = "mbp_id"))
    @Column(name = "description_line")
    private List<String> description = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "mbp_sizes", joinColumns = @JoinColumn(name = "mbp_id"))
    @Column(name = "size")
    private List<String> productSizes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "mbp_features", joinColumns = @JoinColumn(name = "mbp_id"))
    @Column(name = "feature")
    private List<String> features = new ArrayList<>();

    @Lob
    @Column(name = "mbp_main_img", columnDefinition = "LONGBLOB")
    private byte[] productMainImage;

    @ElementCollection
    @CollectionTable(name = "mbp_sub_images", joinColumns = @JoinColumn(name = "mbp_id"))
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private List<byte[]> productSubImages = new ArrayList<>();

    private String specifications;


    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }


    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;


    // Add the new field for inventory batches (mirroring ProductEntity structure)
    @OneToMany(mappedBy = "mbp", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<InventoryEntity> inventoryBatches = new ArrayList<>();

    //NEW DELETED PRODUCT
    public static Specification<MbPEntity> notDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    // Getters & Setters
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

    public byte[] getProductMainImage() { return productMainImage; }
    public void setProductMainImage(byte[] productMainImage) { this.productMainImage = productMainImage; }

    public List<byte[]> getProductSubImages() { return productSubImages; }
    public void setProductSubImages(List<byte[]> productSubImages) { this.productSubImages = productSubImages; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }


    public List<InventoryEntity> getInventoryBatches() {
        return inventoryBatches;
    }

    public void setInventoryBatches(List<InventoryEntity> inventoryBatches) {
        this.inventoryBatches = inventoryBatches;
    }
}