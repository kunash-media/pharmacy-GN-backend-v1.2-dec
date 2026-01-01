package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.BatchWithProductDTO;
import com.gn.pharmacy.dto.response.ProductAdminResponseDTO;
import com.gn.pharmacy.entity.InventoryEntity;
import com.gn.pharmacy.entity.MbPEntity;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.repository.InventoryRepository;
import com.gn.pharmacy.repository.MbPRepository;
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

    @Autowired
    private MbPRepository mbpRepository;  // Make sure this exists

    @Autowired
    private InventoryRepository inventoryRepository;

    // Backward compatibility - only ProductEntity
    @Override
    public void addStockBatchToProduct(Long productId, BatchInfoDTO batchInfo) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        InventoryEntity inventory = new InventoryEntity();
        inventory.setProduct(product);
        inventory.setBatchNo(batchInfo.getBatchNo());
        inventory.setQuantity(batchInfo.getQuantity());
        inventory.setMfgDate(batchInfo.getMfgDate());
        inventory.setExpDate(batchInfo.getExpiryDate());

        inventoryRepository.save(inventory);
    }

    // Unified add batch - supports both ProductEntity and MbPEntity
    @Override
    public void addStockBatch(BatchInfoDTO batchInfo) {
        if ((batchInfo.getProductId() == null && batchInfo.getMbpId() == null) ||
                (batchInfo.getProductId() != null && batchInfo.getMbpId() != null)) {
            throw new IllegalArgumentException("Exactly one of productId or mbpId must be provided.");
        }

        InventoryEntity inventory = new InventoryEntity();

        if (batchInfo.getProductId() != null) {
            ProductEntity product = productRepository.findById(batchInfo.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + batchInfo.getProductId()));
            inventory.setProduct(product);
        } else {
            MbPEntity mbp = mbpRepository.findById(batchInfo.getMbpId())
                    .orElseThrow(() -> new RuntimeException("MbP product not found with ID: " + batchInfo.getMbpId()));
            inventory.setMbp(mbp);
        }

        inventory.setBatchNo(batchInfo.getBatchNo());
        inventory.setQuantity(batchInfo.getQuantity());
        inventory.setMfgDate(batchInfo.getMfgDate());
        inventory.setExpDate(batchInfo.getExpiryDate());
        // stockStatus can be set to default if needed, e.g., "AVAILABLE"

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
        response.setSku(product.getSku());
        response.setBrandName(product.getBrandName());

        List<BatchInfoDTO> batchDTOs = batches.stream()
                .map(batch -> new BatchInfoDTO(
                        batch.getInventoryId(),
                        batch.getBatchNo(),
                        batch.getQuantity(),
                        batch.getMfgDate(),
                        batch.getExpDate(),
                        batch.getStockStatus(),
                        batch.getLastUpdated()
                ))
                .collect(Collectors.toList());

        response.setBatches(batchDTOs);

        int totalStock = batchDTOs.stream().mapToInt(BatchInfoDTO::getQuantity).sum();
        response.setTotalStock(totalStock);

        return response;
    }

    @Override
    public Page<BatchWithProductDTO> getAllBatches(int page, int size, Long productId, Long mbpId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdated").descending());

        Page<InventoryEntity> inventoryPage;

        if (productId != null && mbpId != null) {
            throw new IllegalArgumentException("Only one filter (productId or mbpId) can be applied at a time.");
        } else if (productId != null) {
            inventoryPage = inventoryRepository.findByProductProductId(productId, pageable);
        } else if (mbpId != null) {
            inventoryPage = inventoryRepository.findByMbpId(mbpId, pageable);
        } else {
            inventoryPage = inventoryRepository.findAll(pageable);
        }

        // Calculate total stock per parent item (product or mbp)
        Map<Long, Integer> totalStockMap = inventoryPage.getContent().stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getProduct() != null ? inv.getProduct().getProductId() : inv.getMbp().getId(),
                        Collectors.summingInt(InventoryEntity::getQuantity)
                ));

        return inventoryPage.map(inventory -> {
            ProductEntity prod = inventory.getProduct();
            MbPEntity mbp = inventory.getMbp();

            // Defensive check: exactly one of product or mbp should be present
            if (prod == null && mbp == null) {
                throw new IllegalStateException("Inventory entity must have either a product or an MBP.");
            }
            if (prod != null && mbp != null) {
                throw new IllegalStateException("Inventory entity cannot have both product and MBP set.");
            }

            Long itemId = prod != null ? prod.getProductId() : mbp.getId();
            String itemName = prod != null ? prod.getProductName() : mbp.getTitle();
            String sku = prod != null ? prod.getSku() : mbp.getSku();
            String brandName = prod != null
                    ? prod.getBrandName()
                    : (mbp.getBrand() != null ? mbp.getBrand() : "N/A");

            Integer totalStock = totalStockMap.getOrDefault(itemId, 0);

            return new BatchWithProductDTO(
                    inventory.getInventoryId(),
                    inventory.getBatchNo(),
                    inventory.getQuantity(),
                    inventory.getMfgDate(),
                    inventory.getExpDate(),
                    inventory.getStockStatus(),
                    inventory.getLastUpdated(),
                    prod != null ? prod.getProductId() : null,  // Safe: null if no product
                    mbp != null ? mbp.getId() : null,          // Safe: null if no mbp
                    itemName,
                    sku,
                    brandName,
                    totalStock
            );
        });
    }

    @Override
    @Transactional
    public void updateBatch(Long inventoryId, BatchInfoDTO batchUpdate) {
        InventoryEntity inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + inventoryId));

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

        inventoryRepository.save(inventory); // lastUpdated auto-updated
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