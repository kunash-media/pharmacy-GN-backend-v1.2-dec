package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.BatchWithProductDTO;
import com.gn.pharmacy.dto.response.ProductAdminResponseDTO;
import com.gn.pharmacy.entity.InventoryEntity;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.repository.InventoryRepository;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired private InventoryRepository inventoryRepository;

    @Override
    public void addStockBatch(Long productId, BatchInfoDTO batchInfo) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        InventoryEntity inventory = new InventoryEntity();
        inventory.setProduct(product);
        inventory.setBatchNo(batchInfo.getBatchNo());
        inventory.setQuantity(batchInfo.getQuantity());
        inventory.setExpDate(batchInfo.getExpiryDate());
        inventory.setMfgDate(batchInfo.getMfgDate());

        inventoryRepository.save(inventory);
    }

    @Override
    public ProductAdminResponseDTO getProductStockDetails(Long productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        List<InventoryEntity> batches = inventoryRepository.findByProductProductId(productId);

        ProductAdminResponseDTO response = new ProductAdminResponseDTO();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());
        response.setSku(product.getSku());                    // Now properly set (was missing?)
        response.setBrandName(product.getBrandName());

        List<BatchInfoDTO> batchDTOs = batches.stream()
                .map(batch -> new BatchInfoDTO(
                        batch.getInventoryId(),              // Fixed: now includes ID
                        batch.getBatchNo(),
                        batch.getQuantity(),
                        batch.getMfgDate(),
                        batch.getExpDate(),
                        batch.getStockStatus(),              // Fixed: now includes status
                        batch.getLastUpdated()               // Fixed: now includes timestamp
                ))
                .collect(Collectors.toList());

        response.setBatches(batchDTOs);

        // Calculate total stock from actual batch quantities
        int totalStock = batchDTOs.stream()
                .mapToInt(BatchInfoDTO::getQuantity)
                .sum();
        response.setTotalStock(totalStock);

        return response;
    }

    @Override
    public void updateBatchQuantity(String batchNo, Integer newQuantity) {
        //implementation
    }


    @Override
    public Page<BatchWithProductDTO> getAllBatches(int page, int size, Long productId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdated").descending());

        Page<InventoryEntity> inventoryPage;

        if (productId != null) {
            // Use the new method with JOIN FETCH
            inventoryPage = inventoryRepository.findByProductIdWithProduct(productId, pageable);
        } else {
            // Use the new method with JOIN FETCH
            inventoryPage = inventoryRepository.findAllWithProduct(pageable);
        }

        // Now all products are already loaded, no N+1 queries
        // Calculate total stock per product
        Map<Long, Integer> productTotalStockMap = inventoryPage.getContent()
                .stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getProduct().getProductId(),
                        Collectors.summingInt(InventoryEntity::getQuantity)
                ));

        return inventoryPage.map(inventory -> {
            ProductEntity product = inventory.getProduct();
            Integer totalStock = productTotalStockMap.getOrDefault(product.getProductId(), 0);

            return new BatchWithProductDTO(
                    inventory.getInventoryId(),
                    inventory.getBatchNo(),
                    inventory.getQuantity(),
                    inventory.getMfgDate(),
                    inventory.getExpDate(),
                    inventory.getStockStatus(),
                    inventory.getLastUpdated(),
                    product.getProductId(),
                    product.getProductName(),
                    product.getSku(),
                    product.getBrandName(),
                    totalStock
            );
        });
    }

    @Override
    @Transactional
    public void updateBatch(Long inventoryId, BatchInfoDTO batchUpdate) {
        InventoryEntity inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + inventoryId));

        // Update fields only if provided in the request (true PATCH behavior)
        if (batchUpdate.getBatchNo() != null && !batchUpdate.getBatchNo().trim().isEmpty()) {
            inventory.setBatchNo(batchUpdate.getBatchNo().trim());
        }
        if (batchUpdate.getQuantity() != null) {
            inventory.setQuantity(batchUpdate.getQuantity());
        }
        if (batchUpdate.getMfgDate() != null && !batchUpdate.getMfgDate().trim().isEmpty()) {
            inventory.setMfgDate(batchUpdate.getMfgDate().trim());
        }
        if (batchUpdate.getExpiryDate() != null && !batchUpdate.getExpiryDate().trim().isEmpty()) {
            inventory.setExpDate(batchUpdate.getExpiryDate().trim());
        }
        if (batchUpdate.getStockStatus() != null && !batchUpdate.getStockStatus().trim().isEmpty()) {
            inventory.setStockStatus(batchUpdate.getStockStatus().trim());
        }

        // lastUpdated is automatically updated by @PreUpdate
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public void deleteBatch(Long inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            throw new RuntimeException("Batch not found with ID: " + inventoryId);
        }
        inventoryRepository.deleteById(inventoryId);
    }
}
