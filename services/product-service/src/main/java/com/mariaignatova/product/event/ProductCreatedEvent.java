package com.mariaignatova.product.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCreatedEvent(
        UUID id,
        String name,
        BigDecimal price,
        Integer stock
) {}