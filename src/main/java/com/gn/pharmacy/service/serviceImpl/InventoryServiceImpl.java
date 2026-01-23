package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.BatchWithProductDTO;
import com.gn.pharmacy.dto.response.ProductAdminResponseDTO;
import com.gn.pharmacy.entity.BatchVariant;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MbPRepository mbpRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public void addStockBatchToProduct(Long productId, BatchInfoDTO batchInfo) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        InventoryEntity inventory = new InventoryEntity();
        inventory.setProduct(product);
        inventory.setBatchNo(batchInfo.getBatchNo());

        // Require variants list (modern flow only)
        if (batchInfo.getVariants() == null || batchInfo.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Variants list is required when adding stock batch. No legacy single fields supported.");
        }

        List<BatchVariant> entityVariants = batchInfo.getVariants().stream()
                .map(v -> new BatchVariant(
                        v.getSize(),
                        v.getQuantity(),
                        v.getMfgDate(),
                        v.getExpiryDate()
                ))
                .collect(Collectors.toList());

        inventory.setVariants(entityVariants);
        inventory.setStockStatus("AVAILABLE");
        inventoryRepository.save(inventory);
    }

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

        // Require variants list (modern flow only)
        if (batchInfo.getVariants() == null || batchInfo.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Variants list is required when adding stock batch. No legacy single fields supported.");
        }

        List<BatchVariant> entityVariants = batchInfo.getVariants().stream()
                .map(v -> new BatchVariant(
                        v.getSize(),
                        v.getQuantity(),
                        v.getMfgDate(),
                        v.getExpiryDate()
                ))
                .collect(Collectors.toList());

        inventory.setVariants(entityVariants);
        inventory.setStockStatus("AVAILABLE");
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
                .map(batch -> {
                    BatchInfoDTO dto = new BatchInfoDTO();
                    dto.setInventoryId(batch.getInventoryId());
                    dto.setBatchNo(batch.getBatchNo());
                    dto.setStockStatus(batch.getStockStatus());
                    dto.setLastUpdated(batch.getLastUpdated());

                    if (batch.getVariants() != null && !batch.getVariants().isEmpty()) {
                        List<BatchInfoDTO.VariantDTO> variantDtos = batch.getVariants().stream()
                                .map(v -> new BatchInfoDTO.VariantDTO(
                                        v.getSize(),
                                        v.getQuantity(),
                                        v.getMfgDate(),
                                        v.getExpDate()
                                ))
                                .collect(Collectors.toList());
                        dto.setVariants(variantDtos);

                        // Calculate and set total
                        int batchTotal = batch.getVariants().stream()
                                .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
                                .sum();
                        dto.setTotalQuantity(batchTotal);
                    } else {
                        dto.setVariants(new ArrayList<>());
                        dto.setTotalQuantity(0);
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        response.setBatches(batchDTOs);

        int totalStock = batchDTOs.stream().mapToInt(BatchInfoDTO::getTotalQuantity).sum();
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

        // Total stock per parent (sum of all variants across all batches)
        Map<Long, Integer> totalStockMap = inventoryPage.getContent().stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getProduct() != null ? inv.getProduct().getProductId() : inv.getMbp().getId(),
                        Collectors.summingInt(inv -> inv.getVariants().stream()
                                .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
                                .sum())
                ));

        return inventoryPage.map(inventory -> {
            ProductEntity prod = inventory.getProduct();
            MbPEntity mbp = inventory.getMbp();

            if (prod == null && mbp == null) {
                throw new IllegalStateException("Inventory entity must have either a product or an MBP.");
            }
            if (prod != null && mbp != null) {
                throw new IllegalStateException("Inventory entity cannot have both product and MBP set.");
            }

            Long itemId = prod != null ? prod.getProductId() : mbp.getId();
            String itemName = prod != null ? prod.getProductName() : mbp.getTitle();
            String sku = prod != null ? prod.getSku() : mbp.getSku();
            String brandName = prod != null ? prod.getBrandName() : (mbp.getBrand() != null ? mbp.getBrand() : "N/A");

            Integer totalStock = totalStockMap.getOrDefault(itemId, 0);

            // Batch total from its own variants
            int batchTotal = inventory.getVariants().stream()
                    .mapToInt(v -> v.getQuantity() != null ? v.getQuantity() : 0)
                    .sum();

            return new BatchWithProductDTO(
                    inventory.getInventoryId(),
                    inventory.getBatchNo(),
                    batchTotal,
                    null,   // no batch-level mfgDate
                    null,   // no batch-level expDate
                    inventory.getStockStatus(),
                    inventory.getLastUpdated(),
                    prod != null ? prod.getProductId() : null,
                    mbp != null ? mbp.getId() : null,
                    itemName,
                    sku,
                    brandName,
                    totalStock,
                    null    // no batch-level size
            );
        });
    }

    @Override
    @Transactional
    public void updateBatch(Long inventoryId, BatchInfoDTO batchUpdate) {
        InventoryEntity inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + inventoryId));

        // Update batch-level fields if provided
        if (batchUpdate.getBatchNo() != null && !batchUpdate.getBatchNo().trim().isEmpty()) {
            inventory.setBatchNo(batchUpdate.getBatchNo().trim());
        }

        if (batchUpdate.getStockStatus() != null && !batchUpdate.getStockStatus().trim().isEmpty()) {
            inventory.setStockStatus(batchUpdate.getStockStatus().trim());
        }

        // Replace entire variants list if provided (standard PATCH for collections)
        if (batchUpdate.getVariants() != null) {
            List<BatchVariant> newVariants = batchUpdate.getVariants().stream()
                    .map(v -> new BatchVariant(
                            v.getSize(),
                            v.getQuantity(),
                            v.getMfgDate(),
                            v.getExpiryDate()
                    ))
                    .collect(Collectors.toList());
            inventory.setVariants(newVariants);
        }

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