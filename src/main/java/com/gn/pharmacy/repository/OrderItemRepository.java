package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.OrderItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

    // Add inside OrderItemRepository interface
    @Query("""
    SELECT COALESCE(p.productName, m.title, 'Unknown Product'),
           SUM(oi.quantity),
           SUM(oi.subtotal)
    FROM OrderItemEntity oi
    LEFT JOIN oi.product p
    LEFT JOIN oi.MbP m
    JOIN oi.order o
    WHERE o.orderDate >= :from
    GROUP BY COALESCE(p.productId, m.id)
    ORDER BY SUM(oi.subtotal) DESC
    """)
    List<Object[]> findTopSelling(@Param("from") LocalDateTime from, Pageable pageable);
    // Replace both sumTotalAmount methods with these string-based versions
}