package com.gn.pharmacy.dto.response;

import java.util.List;
import java.util.ArrayList;

public class ProductAdminResponseDTO {

    private Long productId;
    private String productName;
    private String sku;
    private String brandName;
    private Integer totalStock;
    private List<BatchInfoDTO> batches = new ArrayList<>();

    public ProductAdminResponseDTO() {}

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }

    public List<BatchInfoDTO> getBatches() { return batches; }
    public void setBatches(List<BatchInfoDTO> batches) { this.batches = batches; }
}
