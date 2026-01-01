package com.gn.pharmacy.dto.response;

import java.time.LocalDateTime;

public class BatchWithProductDTO {

    private Long inventoryId;
    private String batchNo;
    private Integer quantity;
    private String mfgDate;
    private String expiryDate;
    private String stockStatus;
    private LocalDateTime lastUpdated;

    // Product details
    // Product / MbP details (we'll use generic names)
    private Long mbpId;
    private Long productId;                    // productId OR mbpId
    private String itemName;                // productName or MbP title
    private String sku;                     // same field name in both entities
    private String brandName;              // optional in MbP, can be null
    private Integer productTotalStock;



    // Constructors
    public BatchWithProductDTO() {}


    public BatchWithProductDTO(Long inventoryId, String batchNo, Integer quantity,
                               String mfgDate, String expiryDate, String stockStatus,
                               LocalDateTime lastUpdated, Long mbpId, Long productId,
                               String itemName, String sku, String brandName, Integer productTotalStock) {
        this.inventoryId = inventoryId;
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.mfgDate = mfgDate;
        this.expiryDate = expiryDate;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
        this.mbpId = mbpId;
        this.productId = productId;
        this.itemName = itemName;
        this.sku = sku;
        this.brandName = brandName;
        this.productTotalStock = productTotalStock;
    }


    public Long getMbpId() {
        return mbpId;
    }

    public void setMbpId(Long mbpId) {
        this.mbpId = mbpId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    // All getters and setters
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getMfgDate() { return mfgDate; }
    public void setMfgDate(String mfgDate) { this.mfgDate = mfgDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }


    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public Integer getProductTotalStock() { return productTotalStock; }
    public void setProductTotalStock(Integer productTotalStock) { this.productTotalStock = productTotalStock; }
}