package com.mariaignatova.order.query.controller;

import com.mariaignatova.order.event.store.EventStoreRepository;
import com.mariaignatova.order.event.store.StoredEvent;
import com.mariaignatova.order.query.model.OrderView;
import com.mariaignatova.order.query.repository.OrderViewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Queries (Read)", description = "CQRS read side")
public class OrderQueryController {

    private final OrderViewRepository viewRepository;
    private final EventStoreRepository eventStoreRepository;

    public OrderQueryController(OrderViewRepository viewRepository,
                                EventStoreRepository eventStoreRepository) {
        this.viewRepository = viewRepository;
        this.eventStoreRepository = eventStoreRepository;
    }

    @GetMapping
    @Operation(summary = "Get all orders (query)")
    public List<OrderView> getAll() {
        return viewRepository.findAll();
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID (query)")
    public ResponseEntity<OrderView> getById(@PathVariable ("orderId") UUID orderId) {
        return viewRepository.findById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{orderId}/history")
    @Operation(summary = "Get full event history for an order (Event Store)")
    public List<StoredEvent> getHistory(@PathVariable ("orderId") UUID orderId) {
        return eventStoreRepository.findByAggregateIdOrderByOccurredAt(orderId);
    }
}