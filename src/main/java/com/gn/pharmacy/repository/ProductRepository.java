package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

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
}