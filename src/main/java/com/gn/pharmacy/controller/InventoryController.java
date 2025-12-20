package com.gn.pharmacy.controller;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.ProductAdminResponseDTO;
import com.gn.pharmacy.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    // 1. Get full details of a product with all its batches
    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductAdminResponseDTO> getProductDetails(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductStockDetails(productId));
    }


    // 2. Add a new batch (Inventory) to an existing product
    @PostMapping("/add-batch/{productId}")
    public ResponseEntity<String> addBatch(@PathVariable Long productId, @RequestBody BatchInfoDTO batchInfo) {
        inventoryService.addStockBatch(productId, batchInfo);
        return ResponseEntity.ok("Batch added successfully to the inventory.");
    }
}
