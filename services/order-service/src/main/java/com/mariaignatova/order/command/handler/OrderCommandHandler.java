package com.mariaignatova.order.command.handler;

import com.mariaignatova.order.command.CancelOrderCommand;
import com.mariaignatova.order.command.CreateOrderCommand;
import com.mariaignatova.order.event.OrderCancelledEvent;
import com.mariaignatova.order.event.OrderCreatedEvent;
import com.mariaignatova.order.event.publisher.OrderEventPublisher;
import com.mariaignatova.order.event.store.EventStoreRepository;
import com.mariaignatova.order.event.store.StoredEvent;
import com.mariaignatova.order.exception.OrderNotFoundException;
import com.mariaignatova.order.query.projection.OrderProjection;
import com.mariaignatova.order.query.repository.OrderViewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderCommandHandler {

    private final EventStoreRepository eventStore;
    private final OrderProjection projection;
    private final OrderEventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final OrderViewRepository viewRepository;

    private Counter ordersCreatedCounter;
    private Counter ordersCancelledCounter;

    public OrderCommandHandler(EventStoreRepository eventStore,
                               OrderProjection projection,
                               OrderEventPublisher publisher,
                               ObjectMapper objectMapper,
                               MeterRegistry meterRegistry,
                               OrderViewRepository viewRepository) {
        this.eventStore = eventStore;
        this.projection = projection;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
        this.viewRepository = viewRepository;
    }

    @PostConstruct
    public void initMetrics() {
        ordersCreatedCounter = Counter.builder("orders.created.total")
                .description("Total number of orders created since startup")
                .register(meterRegistry);

        ordersCancelledCounter = Counter.builder("orders.cancelled.total")
                .description("Total number of orders cancelled since startup")
                .register(meterRegistry);

        io.micrometer.core.instrument.Gauge.builder(
                        "orders.active.count", viewRepository,
                        repo -> (double) repo.countByStatus("CREATED"))
                .description("Number of currently active orders")
                .register(meterRegistry);
    }

    @Transactional
    public UUID handle(CreateOrderCommand command) {
        UUID orderId = UUID.randomUUID();
        BigDecimal total = command.pricePerUnit()
                .multiply(BigDecimal.valueOf(command.quantity()));

        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId, command.productId(), command.quantity(), total);

        persistEvent(orderId, "OrderCreatedEvent", event);
        projection.apply(event);
        publisher.publishOrderCreated(event);
        ordersCreatedCounter.increment();

        return orderId;
    }

    @Transactional
    public void handle(CancelOrderCommand command) {
        List<StoredEvent> history = eventStore
                .findByAggregateIdOrderByOccurredAt(command.orderId());

        if (history.isEmpty()) {
            throw new OrderNotFoundException(command.orderId());
        }

        String currentStatus = replayStatus(history);
        if ("CANCELLED".equals(currentStatus)) {
            throw new IllegalStateException("Order is already cancelled: " + command.orderId());
        }

        OrderCancelledEvent event = new OrderCancelledEvent(command.orderId());
        persistEvent(command.orderId(), "OrderCancelledEvent", event);
        projection.apply(event);
        ordersCancelledCounter.increment();
        publisher.publishOrderCancelled(event);
    }

    private void persistEvent(UUID aggregateId, String type, Object payload) {
        try {
            StoredEvent stored = new StoredEvent();
            stored.setAggregateId(aggregateId);
            stored.setEventType(type);
            stored.setPayload(objectMapper.writeValueAsString(payload));
            stored.setOccurredAt(Instant.now());
            eventStore.save(stored);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    private String replayStatus(List<StoredEvent> events) {
        String status = null;
        for (StoredEvent e : events) {
            switch (e.getEventType()) {
                case "OrderCreatedEvent" -> status = "CREATED";
                case "OrderCancelledEvent" -> status = "CANCELLED";
            }
        }
        return status;
    }
}