package com.mariaignatova.product.service;

import com.mariaignatova.product.dto.CreateProductRequest;
import com.mariaignatova.product.dto.ProductDto;
import com.mariaignatova.product.event.ProductCreatedEvent;
import com.mariaignatova.product.model.Product;
import com.mariaignatova.product.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;
    private final ProductCacheService cache;
    private final ProductEventPublisher publisher;
    private final Counter productsCreatedCounter;

    public ProductService(ProductRepository repository,
                          ProductCacheService cache,
                          ProductEventPublisher publisher,
                          MeterRegistry meterRegistry) {
        this.repository = repository;
        this.cache = cache;
        this.publisher = publisher;
        this.productsCreatedCounter = Counter.builder("products.created.total")
                .description("Total number of products created since startup")
                .register(meterRegistry);
    }

    @Transactional
    public ProductDto create(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());

        Product saved = repository.save(product);
        ProductDto dto = toDto(saved);

        publisher.publishCreated(new ProductCreatedEvent(
                saved.getId(), saved.getName(), saved.getPrice(), saved.getStock()));

        productsCreatedCounter.increment();

        return dto;
    }

    public Optional<ProductDto> findById(UUID id) {
        Optional<ProductDto> cached = cache.get(id);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<ProductDto> fromDb = repository.findById(id).map(this::toDto);
        fromDb.ifPresent(cache::put);
        return fromDb;
    }

    public List<ProductDto> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        repository.deleteById(id);
        cache.evict(id);
    }

    private ProductDto toDto(Product p) {
        return new ProductDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock());
    }
}