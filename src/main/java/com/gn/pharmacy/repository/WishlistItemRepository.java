package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItemEntity, Long> {

    // Existing: For MEDICINE products
    Optional<WishlistItemEntity> findByUserAndProduct(UserEntity user, ProductEntity product);

    // NEW: For MOTHER/BABY products
    Optional<WishlistItemEntity> findByUserAndMbP(UserEntity user, MbPEntity mbP);

    // Existing delete
    void deleteByUser(UserEntity user);

    // UPDATED: Fetch all wishlist items with eager loading of both possible product references
    @Query("SELECT w FROM WishlistItemEntity w " +
            "LEFT JOIN FETCH w.product " +
            "LEFT JOIN FETCH w.mbP " +
            "WHERE w.user.id = :userId")
    List<WishlistItemEntity> findByUserIdWithProduct(@Param("userId") Long userId);
}