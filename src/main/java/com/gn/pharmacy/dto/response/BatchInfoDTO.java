package com.gn.pharmacy.dto.response;

import java.time.LocalDateTime;

public class BatchInfoDTO {


    private Long inventoryId;        // New
    private String batchNo;
    private Integer quantity;
    private String mfgDate;
    private String expiryDate;
    private String stockStatus;      // New
    private LocalDateTime lastUpdated;

    public BatchInfoDTO() {}

    public BatchInfoDTO(String batchNo, Integer quantity, String expiryDate, String mfgDate) {
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.mfgDate = mfgDate;
    }

    public BatchInfoDTO(Long inventoryId, String batchNo, Integer quantity,
                        String mfgDate, String expiryDate, String stockStatus,
                        LocalDateTime lastUpdated) {
        this.inventoryId = inventoryId;
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.mfgDate = mfgDate;
        this.expiryDate = expiryDate;
        this.stockStatus = stockStatus;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getMfgDate() { return mfgDate; }
    public void setMfgDate(String mfgDate) { this.mfgDate = mfgDate; }


    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
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
}
