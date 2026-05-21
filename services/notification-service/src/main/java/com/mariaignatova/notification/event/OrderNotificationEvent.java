package com.mariaignatova.notification.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderNotificationEvent(
        UUID orderId,
        UUID productId,
        Integer quantity,
        BigDecimal totalPrice
) {};