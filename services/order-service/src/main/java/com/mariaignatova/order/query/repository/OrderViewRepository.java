package com.mariaignatova.order.query.repository;

import com.mariaignatova.order.query.model.OrderView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderViewRepository extends JpaRepository<OrderView, UUID> {

    long countByStatus(String status);
}