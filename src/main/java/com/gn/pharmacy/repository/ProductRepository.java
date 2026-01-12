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

    // ==================================================================
    // UPDATED: Public "active" products = not deleted AND approved = true
    // ==================================================================

    /**
     * Returns all products that are:
     * - Not deleted (isDeleted = false)
     * - Approved (approved = true)
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.isDeleted = false AND p.isApproved = true")
    List<ProductEntity> findAllActive();

    @Query("SELECT p FROM ProductEntity p WHERE p.isDeleted = false AND p.isApproved = true")
    Page<ProductEntity> findAllActive(Pageable pageable);

    // Optional: Keep old default methods if other parts use them (but they now use the new logic)
    // You can remove these defaults if you prefer to always use the @Query versions above
    /*
    default List<ProductEntity> findAllActive() {
        return findAll(ProductEntity.notDeletedAndApproved());
    }

    default Page<ProductEntity> findAllActive(Pageable pageable) {
        return findAll(ProductEntity.notDeletedAndApproved(), pageable);
    }
    */

    // New method: non-deleted + approved by category
    @Query("SELECT p FROM ProductEntity p WHERE p.productCategory = :category AND p.isDeleted = false AND p.isApproved = true")
    List<ProductEntity> findByProductCategoryAndActive(@Param("category") String category);

    // If you need a version that includes non-approved (for admin), keep this
    @Query("SELECT p FROM ProductEntity p WHERE p.productCategory = :category AND p.isDeleted = false")
    List<ProductEntity> findByProductCategoryAndNotDeleted(@Param("category") String category);


    // 4. By SubCategory – only active & approved
    @Query("SELECT p FROM ProductEntity p WHERE p.productSubCategory = :subCategory AND p.isDeleted = false AND p.isApproved = true")
    List<ProductEntity> findByProductSubCategoryAndActive(@Param("subCategory") String subCategory);


    //reports query

    @Query("SELECT DISTINCT p.productCategory FROM ProductEntity p WHERE p.isDeleted = false")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT p.productSubCategory FROM ProductEntity p WHERE p.productCategory = :category AND p.isDeleted = false")
    List<String> findDistinctSubcategoriesByCategory(@Param("category") String category);
}