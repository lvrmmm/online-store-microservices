package com.mariaignatova.order.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderCommand(
        @NotNull UUID productId,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal pricePerUnit
) {}