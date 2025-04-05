package com.epam.agency.repository;

import com.epam.agency.model.Order;
import com.epam.agency.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    boolean existsByUserIdAndVoucherId(UUID userId, UUID voucherId);

    @Query("SELECT o FROM Order o " +
            "WHERE (o.user.id = :userId OR o.lastCustomerId = :userId) " +
            "AND o.status NOT IN ('DELETED') " +
            "ORDER BY CASE WHEN o.status = 'REGISTERED' THEN 0 " +
            "WHEN o.status = 'CANCELED' THEN 2 ELSE 1 END, o.orderDate DESC")
    List<Order> findByUserId(UUID userId);

    @Query("SELECT o FROM Order o WHERE " +
            "o.status IN ('REGISTERED', 'CANCELED', 'PAID_AND_CANCELED') AND " +
            "(:status IS NULL OR o.status = :status)")
    Page<Order> findAllOrders(@Param("status") OrderStatus status, Pageable pageable);
}
