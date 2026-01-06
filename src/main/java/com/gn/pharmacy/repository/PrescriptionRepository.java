package com.gn.pharmacy.repository;


import com.gn.pharmacy.entity.PrescriptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, Long> {

    Optional<PrescriptionEntity> findByUserUserIdAndPrescriptionId(Long userId, String prescriptionId);

    Optional<PrescriptionEntity> findByPrescriptionId(String prescriptionId);

    Page<PrescriptionEntity> findByUserUserId(Long userId, Pageable pageable);

    Page<PrescriptionEntity> findByOrderStatus(String orderStatus, Pageable pageable);

    List<PrescriptionEntity> findByUserUserId(Long userId);


    @Query("""
    SELECT COALESCE(oi.product.productName, oi.MbP.title),
           SUM(oi.quantity),
           SUM(oi.subtotal)
    FROM OrderItemEntity oi
    JOIN oi.order o
    WHERE o.orderDate >= :from
    GROUP BY COALESCE(oi.product.productId, oi.MbP.id)
    ORDER BY SUM(oi.subtotal) DESC
    """)
    List<Object[]> findTopSelling(@Param("from") LocalDateTime from, Pageable pageable);

    // Add this to PrescriptionRepository
    Page<PrescriptionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);



}
