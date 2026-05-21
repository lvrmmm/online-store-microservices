package com.mariaignatova.order.event;

import java.util.UUID;

public record OrderCancelledEvent(UUID orderId) {}