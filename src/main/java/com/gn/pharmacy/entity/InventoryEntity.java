package com.gn.pharmacy.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_inventory")
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    // Link back to your existing Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = true)
    private ProductEntity product;

    @Column(name = "batch_no", nullable = true)
    private String batchNo;

    @Column(name = "mfg_date")
    private String mfgDate;

    @Column(name = "exp_date")
    private String expDate;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "stock_status") // e.g., "AVAILABLE", "EXPIRED", "DAMAGED"
    private String stockStatus;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbp_id", nullable = true)
    private MbPEntity mbp;


    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Constructors,

    public InventoryEntity(){}

    public InventoryEntity(Long inventoryId, ProductEntity product, String batchNo,
                           String mfgDate, String expDate, Integer quantity,
                           String stockStatus, LocalDateTime lastUpdated, MbPEntity mbp) {
        this.inventoryId = inventoryId;
        this.product = product;
        this.batchNo = batchNo;
        this.mfgDate = mfgDate;
        this.expDate = expDate;
        this.quantity = quantity;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
        this.mbp = mbp;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
}
