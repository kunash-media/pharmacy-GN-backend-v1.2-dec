package com.gn.pharmacy.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "products")
@SQLDelete(sql = "UPDATE products SET is_deleted = true WHERE product_id = ?")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "product_sub_category")
    private String productSubCategory;


    @ElementCollection
    @CollectionTable(name = "products_prices", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "product_price")
    private List<BigDecimal> productPrice = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "products_original_prices", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "product_old_price")
    private List<BigDecimal> productOldPrice = new ArrayList<>();


    @Column(name = "product_stock")
    private String productStock;

    @Column(name = "product_status")
    private String productStatus;

    @Column(name = "product_description")
    private String productDescription;

    private LocalDateTime createdAt;

    @Column(name = "product_quantity")
    private Integer productQuantity;

    @Column(name = "prescription_required")
    private boolean prescriptionRequired;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "mfg_date")
    private String mfgDate;

    @Column(name = "exp_date")
    private String expDate;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "rating")
    private Double rating;

    @ElementCollection
    @CollectionTable(name = "category_path_products", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "category_path")
    private List<String> categoryPath = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_benefits", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "benefit")
    private List<String> benefitsList = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_ingredients", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "ingredient")
    private List<String> ingredientsList = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "product_directions", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "direction")
    private List<String> directionsList = new ArrayList<>();

    @Lob
    @Column(name = "product_main_img", columnDefinition = "LONGBLOB")
    private byte[] productMainImage;

    @ElementCollection
    @CollectionTable(name = "product_sub_images", joinColumns = @JoinColumn(name = "product_id"))
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private List<byte[]> productSubImages;

    @ElementCollection
    @CollectionTable(name = "product_dynamic_fields", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "field_key")
    @Column(name = "field_value")
    private Map<String, String> productDynamicFields;

    @ElementCollection
    @CollectionTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size")
    private List<String> productSizes = new ArrayList<>();

    @Column(name = "is_approved", nullable = false, columnDefinition = "boolean default false")
    private boolean isApproved = false;

    //NEW DELETED PRODUCT FIELD
    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;

    //NEW DELETED PRODUCT
    public static Specification<ProductEntity> notDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }


    //==================  inventory relationship added ===============//
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryEntity> inventoryBatches = new ArrayList<>();

    // Helper method to get total quantity from all batches
    public Integer getTotalCalculatedStock() {
        return inventoryBatches.stream()
                .mapToInt(InventoryEntity::getQuantity)
                .sum();
    }

    // Constructors
    public ProductEntity() {}


    public ProductEntity(Long productId, String sku, String productName, String productCategory,
                         String productSubCategory, List<BigDecimal> productPrice,
                         List<BigDecimal> productOldPrice, String productStock, String productStatus,
                         String productDescription, LocalDateTime createdAt, Integer productQuantity,
                         boolean prescriptionRequired, String brandName, String mfgDate, String expDate,
                         String batchNo, Double rating, List<String> categoryPath, List<String> benefitsList,
                         List<String> ingredientsList, List<String> directionsList, byte[] productMainImage,
                         List<byte[]> productSubImages, Map<String, String> productDynamicFields, List<String> productSizes,
                         boolean isApproved, boolean isDeleted, List<InventoryEntity> inventoryBatches) {
        this.productId = productId;
        this.sku = sku;
        this.productName = productName;
        this.productCategory = productCategory;
        this.productSubCategory = productSubCategory;
        this.productPrice = productPrice;
        this.productOldPrice = productOldPrice;
        this.productStock = productStock;
        this.productStatus = productStatus;
        this.productDescription = productDescription;
        this.createdAt = createdAt;
        this.productQuantity = productQuantity;
        this.prescriptionRequired = prescriptionRequired;
        this.brandName = brandName;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
        this.batchNo = batchNo;
        this.rating = rating;
        this.categoryPath = categoryPath;
        this.benefitsList = benefitsList;
        this.ingredientsList = ingredientsList;
        this.directionsList = directionsList;
        this.productMainImage = productMainImage;
        this.productSubImages = productSubImages;
        this.productDynamicFields = productDynamicFields;
        this.productSizes = productSizes;
        this.isApproved = isApproved;
        this.isDeleted = isDeleted;
        this.inventoryBatches = inventoryBatches;
    }

    // Getters and Setters (all fields)
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

    public List<String> getCategoryPath() { return categoryPath; }
    public void setCategoryPath(List<String> categoryPath) { this.categoryPath = categoryPath; }

    public List<String> getBenefitsList() { return benefitsList; }
    public void setBenefitsList(List<String> benefitsList) { this.benefitsList = benefitsList; }

    public List<String> getIngredientsList() { return ingredientsList; }
    public void setIngredientsList(List<String> ingredientsList) { this.ingredientsList = ingredientsList; }

    public List<String> getDirectionsList() { return directionsList; }
    public void setDirectionsList(List<String> directionsList) { this.directionsList = directionsList; }

    public byte[] getProductMainImage() { return productMainImage; }
    public void setProductMainImage(byte[] productMainImage) { this.productMainImage = productMainImage; }

    public List<byte[]> getProductSubImages() { return productSubImages; }
    public void setProductSubImages(List<byte[]> productSubImages) { this.productSubImages = productSubImages; }

    public Map<String, String> getProductDynamicFields() { return productDynamicFields; }
    public void setProductDynamicFields(Map<String, String> productDynamicFields) { this.productDynamicFields = productDynamicFields; }

    public List<String> getProductSizes() { return productSizes; }
    public void setProductSizes(List<String> productSizes) { this.productSizes = productSizes; }

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

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
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
}