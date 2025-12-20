package com.gn.pharmacy.dto.response;

public class BatchInfoDTO {
    private String batchNo;
    private Integer quantity;
    private String expiryDate;
    private String mfgDate; // Added for completeness in medical panel

    public BatchInfoDTO() {}

    public BatchInfoDTO(String batchNo, Integer quantity, String expiryDate, String mfgDate) {
        this.batchNo = batchNo;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.mfgDate = mfgDate;
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
}
