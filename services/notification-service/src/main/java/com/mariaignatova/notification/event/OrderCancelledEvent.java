package com.mariaignatova.notification.event;

import java.util.UUID;

public record OrderCancelledEvent(UUID orderId) {}