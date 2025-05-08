package com.cheeza.Cheeza.repository;

import com.cheeza.Cheeza.model.Order;
import com.cheeza.Cheeza.model.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByCustomerNameContainingIgnoreCase(String name);

    @Query("SELECT o FROM Order o WHERE o.status NOT IN :statuses")
    List<Order>findActiveOrders(@Param("finalStatus") List<OrderStatus>finalStatues);
}
