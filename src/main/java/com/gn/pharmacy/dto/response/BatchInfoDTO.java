package com.gn.pharmacy.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for adding/updating inventory batches and returning batch details.
 * All stock data (size, quantity, mfg, expiry) is now per-variant inside the variants list.
 * Single-level fields (quantity, size, etc.) have been removed to avoid confusion.
 */
public class BatchInfoDTO {

    // Response-only fields
    private Long inventoryId;           // batch ID (set by system on create)
    private LocalDateTime lastUpdated;  // last updated timestamp (set by system)

    // Request & Response - batch metadata
    private String batchNo;
    private String stockStatus;         // e.g. "AVAILABLE", "LOW", "EXPIRED"

    // Parent context (used in both request and response)
    private Long productId;
    private Long mbpId;

    private Integer totalQuantity;


    // Core data: list of variants (this is now the only way to send stock info)
    private List<VariantDTO> variants = new ArrayList<>();

    // ────────────────────────────────────────────────
    // Constructors
    // ────────────────────────────────────────────────

    public BatchInfoDTO() {
        // Default no-arg constructor
    }

    // Convenient constructor for creating single-variant batch (backward compatible / simple cases)
    public BatchInfoDTO(String batchNo, Integer quantity, String mfgDate, String expiryDate, String size) {
        this.batchNo = batchNo;
        this.variants.add(new VariantDTO(size, quantity, mfgDate, expiryDate));
    }

    // Full constructor - mostly used when mapping from entity to DTO

    public BatchInfoDTO(Long inventoryId, LocalDateTime lastUpdated, String batchNo, String stockStatus, Long productId, Long mbpId, Integer totalQuantity, List<VariantDTO> variants) {
        this.inventoryId = inventoryId;
        this.lastUpdated = lastUpdated;
        this.batchNo = batchNo;
        this.stockStatus = stockStatus;
        this.productId = productId;
        this.mbpId = mbpId;
        this.totalQuantity = totalQuantity;
        this.variants = variants;
    }


    // ────────────────────────────────────────────────
    // Inner VariantDTO - contains all per-variant details
    // ────────────────────────────────────────────────

    public static class VariantDTO {
        private String size;        // e.g. "S", "M", "Strip of 10", null for no-size
        private Integer quantity;
        private String mfgDate;     // nullable
        private String expiryDate;  // nullable

        public VariantDTO() {}

        public VariantDTO(String size, Integer quantity, String mfgDate, String expiryDate) {
            this.size = size;
            this.quantity = quantity;
            this.mfgDate = mfgDate;
            this.expiryDate = expiryDate;
        }

        // Getters & Setters
        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public String getMfgDate() { return mfgDate; }
        public void setMfgDate(String mfgDate) { this.mfgDate = mfgDate; }

        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    }

    // ────────────────────────────────────────────────
    // Getters & Setters
    // ────────────────────────────────────────────────

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getMbpId() {
        return mbpId;
    }

    public void setMbpId(Long mbpId) {
        this.mbpId = mbpId;
    }

    public List<VariantDTO> getVariants() {
        return variants;
    }


    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public void setVariants(List<VariantDTO> variants) {
        this.variants = variants != null ? new ArrayList<>(variants) : new ArrayList<>();
    }

    // Helper method: total quantity across all variants in this batch
    public Integer getTotalQuantity() {
        return variants.stream()
                .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
                .sum();
    }
}