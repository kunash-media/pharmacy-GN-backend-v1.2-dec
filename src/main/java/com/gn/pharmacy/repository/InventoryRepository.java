package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    // Find all batches for a specific product
    List<InventoryEntity> findByProductProductId(Long productId);

    // Check if a batch number already exists for a product to prevent duplicates
    boolean existsByBatchNoAndProductProductId(String batchNo, Long productId);
}
