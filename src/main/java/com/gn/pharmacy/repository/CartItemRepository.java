package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.CartItemEntity;
import com.gn.pharmacy.entity.MbPEntity;
import com.gn.pharmacy.entity.ProductEntity;
import com.gn.pharmacy.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    List<CartItemEntity> findByUser(UserEntity user);

    Optional<CartItemEntity> findByUserAndProductAndSelectedSize(UserEntity user, ProductEntity product, String selectedSize);
    Optional<CartItemEntity> findByUserAndMbpAndSelectedSize(UserEntity user, MbPEntity mbp, String selectedSize);

    @Query("SELECT c FROM CartItemEntity c WHERE c.user = :user AND c.product.productId = :productId AND c.selectedSize = :size")
    Optional<CartItemEntity> findByUserAndProductIdAndSize(@Param("user") UserEntity user, @Param("productId") Long productId, @Param("size") String size);

    @Query("SELECT c FROM CartItemEntity c WHERE c.user = :user AND c.mbp.id = :mbpId AND c.selectedSize = :size")
    Optional<CartItemEntity> findByUserAndMbpIdAndSize(@Param("user") UserEntity user, @Param("mbpId") Long mbpId, @Param("size") String size);

    // UPDATED: Use UserEntity instead of userId
    @Query("SELECT c FROM CartItemEntity c " +
            "LEFT JOIN FETCH c.product p " +
            "LEFT JOIN FETCH c.mbp m " +
            "WHERE c.user = :user")
    List<CartItemEntity> findByUserWithDetails(@Param("user") UserEntity user);

    void deleteByUser(UserEntity user);

    int countByUser(UserEntity user);
}