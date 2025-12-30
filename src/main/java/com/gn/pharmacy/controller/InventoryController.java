package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.BatchWithProductDTO;
import com.gn.pharmacy.dto.response.ProductAdminResponseDTO;
import com.gn.pharmacy.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // ==================== EXISTING ENDPOINTS (UNCHANGED FOR BACKWARD COMPATIBILITY) ====================

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductAdminResponseDTO> getProductDetails(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductStockDetails(productId));
    }

    @PostMapping("/add-batch/{productId}")
    public ResponseEntity<String> addBatchToProduct(@PathVariable Long productId, @RequestBody BatchInfoDTO batchInfo) {
        inventoryService.addStockBatchToProduct(productId, batchInfo);
        return ResponseEntity.ok("Batch added successfully to the product.");
    }

    // ==================== NEW / UPDATED ENDPOINTS (SUPPORT BOTH PRODUCT & MBP) ====================

    /**
     * Unified endpoint to add batch - accepts either productId or mbpId in the DTO
     */
    @PostMapping("/add-batch")
    public ResponseEntity<String> addBatch(@RequestBody BatchInfoDTO batchInfo) {
        inventoryService.addStockBatch(batchInfo);
        return ResponseEntity.ok("Batch added successfully.");
    }

    /**
     * Get all batches with pagination and optional filtering by productId OR mbpId
     */
    @GetMapping("/get-all-batches")
    public ResponseEntity<Map<String, Object>> getAllBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long mbpId) {

        Page<BatchWithProductDTO> resultPage = inventoryService.getAllBatches(page, size, productId, mbpId);

        Map<String, Object> response = new HashMap<>();
        response.put("data", resultPage.getContent());
        response.put("currentPage", resultPage.getNumber());
        response.put("totalItems", resultPage.getTotalElements());
        response.put("totalPages", resultPage.getTotalPages());
        response.put("pageSize", resultPage.getSize());
        response.put("hasNext", resultPage.hasNext());
        response.put("hasPrevious", resultPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * Update a specific batch (PATCH behavior)
     */
    @PatchMapping("/update-batch-by-inventoryId/{inventoryId}")
    public ResponseEntity<String> updateBatch(
            @PathVariable Long inventoryId,
            @RequestBody BatchInfoDTO batchUpdate) {

        inventoryService.updateBatch(inventoryId, batchUpdate);
        return ResponseEntity.ok("Batch updated successfully.");
    }

    /**
     * Delete a specific batch
     */
    @DeleteMapping("/delete-batch/{inventoryId}")
    public ResponseEntity<String> deleteBatch(@PathVariable Long inventoryId) {
        inventoryService.deleteBatch(inventoryId);
        return ResponseEntity.ok("Batch deleted successfully.");
    }
}