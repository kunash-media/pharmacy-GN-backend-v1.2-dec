package com.gn.pharmacy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_inventory")
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbp_id", nullable = true)
    private MbPEntity mbp;

    @Column(name = "batch_no", nullable = true)
    private String batchNo;

    @Column(name = "stock_status")
    private String stockStatus;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // ────────────── NEW: Replace single size/quantity with collection ──────────────
    @ElementCollection
    @CollectionTable(name = "inventory_variants", joinColumns = @JoinColumn(name = "inventory_id"))
    private List<BatchVariant> variants = new ArrayList<>();

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
     // Helper: total quantity
    public int getTotalQuantity() {
        return variants.stream().mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0).sum();
    }


    // Constructors,
    public InventoryEntity(){}



    public InventoryEntity(Long inventoryId, ProductEntity product, MbPEntity mbp, String batchNo, String stockStatus, LocalDateTime lastUpdated, List<BatchVariant> variants) {
        this.inventoryId = inventoryId;
        this.product = product;
        this.mbp = mbp;
        this.batchNo = batchNo;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
        this.variants = variants;
    }


    // Getters, Setters

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }


    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public MbPEntity getMbp() {
        return mbp;
    }

    public void setMbp(MbPEntity mbp) {
        this.mbp = mbp;
    }


    public List<BatchVariant> getVariants() {
        return variants;
    }

    public void setVariants(List<BatchVariant> variants) {
        this.variants = variants;
    }
}


