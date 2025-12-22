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

    // Existing endpoints (unchanged)
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductAdminResponseDTO> getProductDetails(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductStockDetails(productId));
    }

    @PostMapping("/add-batch/{productId}")
    public ResponseEntity<String> addBatch(@PathVariable Long productId, @RequestBody BatchInfoDTO batchInfo) {
        inventoryService.addStockBatch(productId, batchInfo);
        return ResponseEntity.ok("Batch added successfully to the inventory.");
    }

    // ==================== NEW ENDPOINTS ====================

    // 1. Get all inventory batches with pagination & optional product filter
    @GetMapping("/get-all-batches")
    public ResponseEntity<Map<String, Object>> getAllBatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId) {

        Page<BatchWithProductDTO> resultPage = inventoryService.getAllBatches(page, size, productId);

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

    // 2. Update (PATCH) a specific batch (e.g., quantity, dates, status)
    @PatchMapping("/update-batch-by-inventoryId/{inventoryId}")
    public ResponseEntity<String> updateBatch(
            @PathVariable Long inventoryId,
            @RequestBody BatchInfoDTO batchUpdate) {

        inventoryService.updateBatch(inventoryId, batchUpdate);
        return ResponseEntity.ok("Batch updated successfully.");
    }

    // 3. Delete a specific batch
    @DeleteMapping("/delete-batch/{inventoryId}")
    public ResponseEntity<String> deleteBatch(@PathVariable Long inventoryId) {
        inventoryService.deleteBatch(inventoryId);
        return ResponseEntity.ok("Batch deleted successfully.");
    }
}
