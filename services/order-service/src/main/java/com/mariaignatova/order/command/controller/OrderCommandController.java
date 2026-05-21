package com.mariaignatova.order.command.controller;

import com.mariaignatova.order.command.CancelOrderCommand;
import com.mariaignatova.order.command.CreateOrderCommand;
import com.mariaignatova.order.command.handler.OrderCommandHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Commands (Write)", description = "CQRS write side")
public class OrderCommandController {

    private final OrderCommandHandler commandHandler;

    public OrderCommandController(OrderCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostMapping
    @Operation(summary = "Create a new order (command)")
    public ResponseEntity<Map<String, UUID>> create(@RequestBody @Valid CreateOrderCommand command) {
        UUID orderId = commandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("orderId", orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel an existing order (command)")
    public void cancel(@PathVariable ("orderId") UUID orderId) {
        commandHandler.handle(new CancelOrderCommand(orderId));
    }
}