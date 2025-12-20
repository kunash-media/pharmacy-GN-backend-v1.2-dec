package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.ProductAdminResponseDTO;

public interface InventoryService {
    // Add a new batch to an existing medicine
    void addStockBatch(Long productId, BatchInfoDTO batchInfo);

    // Get the combined Admin View for a medicine
    ProductAdminResponseDTO getProductStockDetails(Long productId);

    // Update quantity of a specific batch
    void updateBatchQuantity(String batchNo, Integer newQuantity);
}
