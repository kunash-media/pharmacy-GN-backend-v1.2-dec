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
    private Long productId;
    private String productName;
    private String sku;
    private String brandName;
    private Integer productTotalStock;  // Total stock across all batches of this product

    // Constructors
    public BatchWithProductDTO() {}

    public BatchWithProductDTO(Long inventoryId, String batchNo, Integer quantity,
                               String mfgDate, String expiryDate, String stockStatus,
                               LocalDateTime lastUpdated, Long productId, String productName,
                               String sku, String brandName, Integer productTotalStock) {
        this.inventoryId = inventoryId;
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.mfgDate = mfgDate;
        this.expiryDate = expiryDate;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.brandName = brandName;
        this.productTotalStock = productTotalStock;
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

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public Integer getProductTotalStock() { return productTotalStock; }
    public void setProductTotalStock(Integer productTotalStock) { this.productTotalStock = productTotalStock; }
}