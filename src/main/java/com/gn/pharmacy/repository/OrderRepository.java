package com.gn.pharmacy.repository;

import com.gn.pharmacy.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>, JpaSpecificationExecutor<OrderEntity> {

    // === ADD TO OrderRepository.java ===
    Page<OrderEntity> findByUser_UserId(Long userId, Pageable pageable);



    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o WHERE o.orderStatus = :status")
    Optional<BigDecimal> sumTotalAmountByStatus(@Param("status") String status);

    // New: Works with orderDate as String (assuming format YYYY-MM-DD or similar)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) " +
            "FROM OrderEntity o " +
            "WHERE SUBSTRING(o.orderDate, 1, 4) = :yearStr " +
            "AND SUBSTRING(o.orderDate, 6, 2) = :monthStr")
    Optional<BigDecimal> sumTotalAmountByMonth(@Param("yearStr") String yearStr, @Param("monthStr") String monthStr);

}