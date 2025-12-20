package com.gn.pharmacy.service.serviceImpl;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.dto.response.ProductAdminResponseDTO;
import com.gn.pharmacy.entity.InventoryEntity;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.repository.InventoryRepository;
import com.gn.pharmacy.repository.ProductRepository;
import com.gn.pharmacy.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<InventoryEntity> batches = inventoryRepository.findByProductProductId(productId);

        ProductAdminResponseDTO response = new ProductAdminResponseDTO();
        response.setProductId(product.getProductId());
        response.setProductName(product.getProductName());
        response.setSku(product.getSku());
        response.setBrandName(product.getBrandName());

        List<BatchInfoDTO> batchDTOs = batches.stream().map(b ->
                new BatchInfoDTO(b.getBatchNo(), b.getQuantity(), b.getExpDate(), b.getMfgDate())
        ).collect(Collectors.toList());

        response.setBatches(batchDTOs);
        response.setTotalStock(batchDTOs.stream().mapToInt(BatchInfoDTO::getQuantity).sum());

        return response;
    }

    @Override
    public void updateBatchQuantity(String batchNo, Integer newQuantity) {
        // Implementation for adjusting stock during sales or returns
    }
}
