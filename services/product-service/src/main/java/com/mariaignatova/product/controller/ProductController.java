package com.mariaignatova.product.controller;

import com.mariaignatova.product.dto.CreateProductRequest;
import com.mariaignatova.product.dto.ProductDto;
import com.mariaignatova.product.service.ProductService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;
    private final MeterRegistry meterRegistry;

    public ProductController(ProductService productService, MeterRegistry meterRegistry) {
        this.productService = productService;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductDto> create(@RequestBody @Valid CreateProductRequest request) {
        Timer timer = meterRegistry.timer("api.product.request.duration", "method", "POST");
        ProductDto created = timer.record(() -> productService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDto> getById(@PathVariable ("id") UUID id) {
        Timer timer = meterRegistry.timer("api.product.request.duration", "method", "GET");
        return timer.record(() -> productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()));
    }

    @GetMapping
    @Operation(summary = "Get all products")
    public List<ProductDto> getAll() {
        return productService.findAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product by ID")
    public void delete(@PathVariable ("id") UUID id) {
        productService.delete(id);
    }
}