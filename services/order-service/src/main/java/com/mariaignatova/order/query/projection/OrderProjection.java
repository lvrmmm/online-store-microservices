package com.mariaignatova.order.query.projection;

import com.mariaignatova.order.event.OrderCancelledEvent;
import com.mariaignatova.order.event.OrderCreatedEvent;
import com.mariaignatova.order.exception.OrderNotFoundException;
import com.mariaignatova.order.query.model.OrderView;
import com.mariaignatova.order.query.repository.OrderViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class OrderProjection {

    private final OrderViewRepository viewRepository;

    public OrderProjection(OrderViewRepository viewRepository) {
        this.viewRepository = viewRepository;
    }

    @Transactional
    public void apply(OrderCreatedEvent event) {
        OrderView view = new OrderView();
        view.setId(event.orderId());
        view.setProductId(event.productId());
        view.setQuantity(event.quantity());
        view.setTotalPrice(event.totalPrice());
        view.setStatus("CREATED");
        view.setCreatedAt(Instant.now());
        view.setUpdatedAt(Instant.now());
        viewRepository.save(view);
    }

    @Transactional
    public void apply(OrderCancelledEvent event) {
        OrderView view = viewRepository.findById(event.orderId())
                .orElseThrow(() -> new OrderNotFoundException(event.orderId()));
        view.setStatus("CANCELLED");
        view.setUpdatedAt(Instant.now());
        viewRepository.save(view);
    }
}