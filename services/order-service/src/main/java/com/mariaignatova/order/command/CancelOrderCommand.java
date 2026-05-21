package com.mariaignatova.order.command;

import java.util.UUID;

public record CancelOrderCommand(UUID orderId) {}