package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.MbPEntity;
import com.gn.pharmacy.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MbPRepository extends JpaRepository<MbPEntity, Long>,
        JpaSpecificationExecutor<MbPEntity>{

    Optional<MbPEntity> findBySku(String sku);
    boolean existsBySku(String sku);
    List<MbPEntity> findByCategory(String category);
    List<MbPEntity> findBySubCategory(String subCategory);
    List<MbPEntity> findByTitleContainingIgnoreCase(String keyword);
    List<MbPEntity> findBySkuContainingIgnoreCase(String keyword);
    List<MbPEntity> findByBrandContainingIgnoreCase(String keyword);
    List<MbPEntity> findByBrand(String brand);
    List<MbPEntity> findByInStockTrue();
    List<MbPEntity> findByInStockFalse();
    List<MbPEntity> findByCategoryAndSubCategory(String category, String subCategory);
    List<MbPEntity> findByCategoryAndBrand(String category, String brand);
    List<MbPEntity> findByPriceBetween(Double minPrice, Double maxPrice);
    List<MbPEntity> findByPriceLessThanEqual(Double maxPrice);
    List<MbPEntity> findByPriceGreaterThanEqual(Double minPrice);
    List<MbPEntity> findByDiscountGreaterThanEqual(Integer minDiscount);
    List<MbPEntity> findByRatingGreaterThanEqual(Double minRating);
    List<MbPEntity> findByRatingBetween(Double minRating, Double maxRating);
    List<MbPEntity> findByStockQuantityGreaterThan(Integer minStock);
    List<MbPEntity> findByStockQuantityLessThanEqual(Integer maxStock);
    List<MbPEntity> findByStockQuantityLessThanEqualAndInStockTrue(Integer threshold);
    List<MbPEntity> findAllByOrderByPriceAsc();
    List<MbPEntity> findAllByOrderByPriceDesc();
    List<MbPEntity> findAllByOrderByRatingDesc();
    List<MbPEntity> findAllByOrderByDiscountDesc();
    List<MbPEntity> findAllByOrderByIdDesc();
    List<MbPEntity> findByCategoryOrderByTitleAsc(String category);


    List<MbPEntity> findByApprovedTrue();
    List<MbPEntity> findByApprovedFalse();
    List<MbPEntity> findByCategoryAndApprovedTrue(String category);
    List<MbPEntity> findByCategoryAndApprovedFalse(String category);
    long countByApprovedTrue();
    long countByApprovedFalse();

    // Override default delete methods to prevent accidental hard deletes
    @Override
    @Modifying
    @Query("UPDATE MbPEntity p SET p.isDeleted = true WHERE p.id = ?1")
    void deleteById(Long id);

    @Override
    @Modifying
    @Query("UPDATE MbPEntity p SET p.isDeleted = true WHERE p = ?1")
    void delete(MbPEntity entity);

    // Include deleted products when needed (for admin purposes)
    @Query("SELECT p FROM MbPEntity p WHERE p.id = ?1 AND p.isDeleted = true")
    Optional<MbPEntity> findDeletedById(Long id);

    default List<MbPEntity> findAllActive() {
        return findAll(MbPEntity.notDeleted());
    }

    default Page<MbPEntity> findAllActive(Pageable pageable) {
        return findAll(MbPEntity.notDeleted(), pageable);
    }

    // New method to fetch non-deleted products by category
    @Query("SELECT p FROM MbPEntity p WHERE p.category = :category AND p.isDeleted = false")
    List<ProductEntity> findByProductCategoryAndNotDeleted(@Param("category") String category);

}