package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long>,
        JpaSpecificationExecutor<ProductEntity> {

    List<ProductEntity> findByProductCategory(String productCategory);

    List<ProductEntity> findByProductSubCategory(String productSubProduct);

    boolean existsByProductName(String productName);

    @Query(value = """
        SELECT p.* FROM products p
        JOIN category_path_products cp ON p.product_id = cp.product_id
        WHERE cp.category_path IN :path
        GROUP BY p.product_id
        HAVING COUNT(DISTINCT cp.category_path) = :pathSize
        """, nativeQuery = true)
    List<ProductEntity> findByCategoryPath(@Param("path") List<String> path, @Param("pathSize") long pathSize);

    @Query(value = """
        SELECT DISTINCT p.* FROM products p
        JOIN category_path_products cp ON p.product_id = cp.product_id
        WHERE cp.category_path = :subPath
        """, nativeQuery = true)
    List<ProductEntity> findByCategoryPathContaining(@Param("subPath") String subPath);

    Optional<ProductEntity> findBySku(String sku);


    // Override default delete methods to prevent accidental hard deletes
    @Override
    @Modifying
    @Query("UPDATE ProductEntity p SET p.isDeleted = true WHERE p.productId = ?1")
    void deleteById(Long productId);

    @Override
    @Modifying
    @Query("UPDATE ProductEntity p SET p.isDeleted = true WHERE p = ?1")
    void delete(ProductEntity entity);

    // Include deleted products when needed (for admin purposes)
    @Query("SELECT p FROM ProductEntity p WHERE p.productId = ?1 AND p.isDeleted = true")
    Optional<ProductEntity> findDeletedById(Long productId);

    default List<ProductEntity> findAllActive() {
        return findAll(ProductEntity.notDeleted());
    }

    default Page<ProductEntity> findAllActive(Pageable pageable) {
        return findAll(ProductEntity.notDeleted(), pageable);
    }

    // New method to fetch non-deleted products by category
    @Query("SELECT p FROM ProductEntity p WHERE p.productCategory = :category AND p.isDeleted = false")
    List<ProductEntity> findByProductCategoryAndNotDeleted(@Param("category") String category);

}