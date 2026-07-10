package com.cropdeal.order.repository;

import com.cropdeal.order.model.Order;
import com.cropdeal.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByDealerId(UUID dealerId);

    List<Order> findByFarmerId(UUID farmerId);

    List<Order> findByStatus(OrderStatus status);
}