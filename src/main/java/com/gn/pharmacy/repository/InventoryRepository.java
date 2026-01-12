package com.gn.pharmacy.repository;

import com.gn.pharmacy.dto.response.BatchInfoDTO;
import com.gn.pharmacy.entity.InventoryEntity;
import com.gn.pharmacy.entity.MbPEntity;
import com.gn.pharmacy.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long>, JpaSpecificationExecutor<InventoryEntity> {

    // Find all batches for a specific product
    List<InventoryEntity> findByProductProductId(Long productId);

    // Check if a batch number already exists for a product to prevent duplicates
    boolean existsByBatchNoAndProductProductId(String batchNo, Long productId);


    // Add this for pagination when filtering by product
    Page<InventoryEntity> findByProductProductId(Long productId, Pageable pageable);


    // New method with JOIN FETCH
    @Query("SELECT i FROM InventoryEntity i JOIN FETCH i.product WHERE i.product.productId = :productId")
    Page<InventoryEntity> findByProductIdWithProduct(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT i FROM InventoryEntity i JOIN FETCH i.product")
    Page<InventoryEntity> findAllWithProduct(Pageable pageable);


    Page<InventoryEntity> findByMbpId(Long mbpId, Pageable pageable);


    // Optional: for performance if you have many batches
    Optional<InventoryEntity> findFirstByProductOrderByLastUpdatedDesc(ProductEntity product);
    Optional<InventoryEntity> findFirstByMbpOrderByLastUpdatedDesc(MbPEntity mbp);


    List<InventoryEntity> findByProduct(ProductEntity product);

    List<InventoryEntity> findByMbp(MbPEntity mbp);

   }
