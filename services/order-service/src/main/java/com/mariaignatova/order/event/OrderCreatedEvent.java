package com.mariaignatova.order.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        UUID productId,
        Integer quantity,
        BigDecimal totalPrice
) {}