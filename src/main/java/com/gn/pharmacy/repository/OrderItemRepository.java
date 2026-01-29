package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.OrderItemEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long>, JpaSpecificationExecutor<OrderItemEntity> {

    @Query(value = """
    SELECT 
        COALESCE(p.product_name, m.title, 'Unknown Product') as product_name,
        SUM(oi.quantity) as total_quantity,
        SUM(oi.subtotal) as total_revenue
    FROM order_items oi
    LEFT JOIN products p ON oi.product_id = p.product_id
    LEFT JOIN mb_products m ON oi.mbp_id = m.id
    JOIN orders_table o ON oi.order_id = o.order_id
    WHERE STR_TO_DATE(o.order_date, '%d/%m/%Y %h:%i %p') >= :fromDate
    GROUP BY COALESCE(p.product_name, m.title, 'Unknown Product')
    ORDER BY total_revenue DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findTopSelling(
            @Param("fromDate") LocalDateTime fromDate,
            @Param("limit") int limit
    );
}