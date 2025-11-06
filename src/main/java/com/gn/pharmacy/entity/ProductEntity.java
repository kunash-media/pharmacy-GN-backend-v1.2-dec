package com.gn.pharmacy.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_category")
    private String productCategory;

    @Column(name = "product_sub_category")
    private String productSubCategory;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    @Column(name = "product_old_price")
    private BigDecimal productOldPrice;

    @Column(name = "product_stock")
    private String productStock;

    @Column(name = "product_status")
    private String productStatus;

    @Column(name = "product_description")
    private String productDescription;

    private LocalDateTime createdAt;

    @Column(name = "product_quantity")
    private Integer productQuantity;

    //NEW ADDED COLUMN
    @Column(name = "prescription_required")
    private boolean prescriptionRequired;

    //NEW ADDED COLUMN
    @Column(name = "brand_name")
    private String brandName;

    //NEW ADDED COLUMN
    @Column(name = "mfgDate")
    private String mfgDate;

    //NEW ADDED COLUMN
    @Column(name = "expDate")
    private String expDate;

    //NEW ADDED COLUMN
    @Column(name = "batchNo")
    private String batchNo;


    // NEW: Hierarchical path
    @ElementCollection
    @CollectionTable(name = "category_path_products", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "category_path")
    private List<String> categoryPath = new ArrayList<>();

    //NEW ADDED COLUMN
    @ElementCollection
    @CollectionTable(name = "product_benefits", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "benefits_list")
    private List<String> benefitsList = new ArrayList<>();

    //NEW ADDED COLUMN
    @ElementCollection
    @CollectionTable(name = "product_ingredients", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "ingredients_list")
    private List<String> ingredientsList = new ArrayList<>();


    @Lob
    @Column(name="product_main_img", columnDefinition = "LONGBLOB")
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


    public ProductEntity(){}

    public ProductEntity(Long productId, String productName, String productCategory, String productSubCategory,
                         BigDecimal productPrice, BigDecimal productOldPrice, String productStock,
                         String productStatus, String productDescription, LocalDateTime createdAt,
                         Integer productQuantity, boolean prescriptionRequired, String brandName,
                         String mfgDate, String expDate, String batchNo, List<String> categoryPath,
                         List<String> benefitsList, List<String> ingredientsList,
                         byte[] productMainImage, List<byte[]> productSubImages,
                         Map<String, String> productDynamicFields, List<String> productSizes) {
        this.productId = productId;
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
        this.categoryPath = categoryPath;
        this.benefitsList = benefitsList;
        this.ingredientsList = ingredientsList;
        this.productMainImage = productMainImage;
        this.productSubImages = productSubImages;
        this.productDynamicFields = productDynamicFields;
        this.productSizes = productSizes;
    }


    // Getters and Setters


    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public BigDecimal getProductOldPrice() {
        return productOldPrice;
    }

    public void setProductOldPrice(BigDecimal productOldPrice) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(Integer productQuantity) {
        this.productQuantity = productQuantity;
    }

    public byte[] getProductMainImage() {
        return productMainImage;
    }

    public void setProductMainImage(byte[] productMainImage) {
        this.productMainImage = productMainImage;
    }

    public List<byte[]> getProductSubImages() {
        return productSubImages;
    }

    public void setProductSubImages(List<byte[]> productSubImages) {
        this.productSubImages = productSubImages;
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

    public List<String> getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(List<String> categoryPath) {
        this.categoryPath = categoryPath;
    }
}