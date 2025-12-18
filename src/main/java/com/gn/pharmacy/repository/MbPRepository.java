package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.MbPEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MbPRepository extends JpaRepository<MbPEntity, Long> {

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
}