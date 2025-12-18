package com.gn.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_items")
public class WishlistItemEntity {
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Mbp_id")
    private MbPEntity mbP;

    @Column(name = "added_date")
    private LocalDateTime addedDate = LocalDateTime.now();

    // NEW: Added ProductType exactly like reference
    public enum ProductType {
        MEDICINE, MOTHER, BABY
    }

    @Column(name = "product_type")
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    // Existing getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public MbPEntity getMbP() {
        return mbP;
    }

    public void setMbP(MbPEntity mbP) {
        this.mbP = mbP;
    }

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }
}





