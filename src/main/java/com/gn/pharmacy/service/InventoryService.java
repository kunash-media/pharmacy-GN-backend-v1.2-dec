package com.gn.pharmacy.service;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.BatchWithProductDTO;
import com.gn.pharmacy.dto.response.ProductAdminResponseDTO;
import org.springframework.data.domain.Page;

public interface InventoryService {
    // Existing
    ProductAdminResponseDTO getProductStockDetails(Long productId);

    // Backward compatible - only for ProductEntity
    void addStockBatchToProduct(Long productId, BatchInfoDTO batchInfo);

    // New unified methods
    void addStockBatch(BatchInfoDTO batchInfo);

    Page<BatchWithProductDTO> getAllBatches(int page, int size, Long productId, Long mbpId);

    void updateBatch(Long inventoryId, BatchInfoDTO batchUpdate);

    void deleteBatch(Long inventoryId);
}
