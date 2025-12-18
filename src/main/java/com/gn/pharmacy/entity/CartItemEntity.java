package com.gn.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "cart_items")
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // --- Connect to ProductEntity ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    // --- Connect to MbPEntity ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbp_id")
    private MbPEntity mbp;

    // --- New: Product Type field ---
    @Column(name = "product_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "selected_size", nullable = true)
    private String selectedSize;

    @Column(name = "added_date")
    private LocalDateTime addedDate = LocalDateTime.now();

    // Product Type Enum
    public enum ProductType {
        MEDICINE, MOTHER, BABY
    }

    // Constructors
    public CartItemEntity() {}

    public CartItemEntity(Long id, UserEntity user, ProductEntity product, MbPEntity mbp, ProductType productType, Integer quantity, String selectedSize, LocalDateTime addedDate) {
        this.id = id;
        this.user = user;
        this.product = product;
        this.mbp = mbp;
        this.productType = productType;
        this.quantity = quantity;
        this.selectedSize = selectedSize;
        this.addedDate = addedDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ProductEntity getProduct() { return product; }
    public void setProduct(ProductEntity product) { this.product = product; }

    public MbPEntity getMbp() { return mbp; }
    public void setMbp(MbPEntity mbp) { this.mbp = mbp; }

    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getSelectedSize() { return selectedSize; }
    public void setSelectedSize(String selectedSize) { this.selectedSize = selectedSize; }

    public LocalDateTime getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDateTime addedDate) { this.addedDate = addedDate; }
}