package edu.dosw.parcial.core.services;

import org.springframework.stereotype.Service;
import edu.dosw.parcial.core.exceptions.ConflictException;
import edu.dosw.parcial.core.models.Product;
import edu.dosw.parcial.persistence.entities.ProductEntity;
import edu.dosw.parcial.persistence.mappers.ProductEntityMapper;
import edu.dosw.parcial.persistence.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductEntityMapper productEntityMapper;

    public Product createProduct(Product product) {
        if (productRepository.existsByNameIgnoreCase(product.getName())) {
            log.warn("Ya existe un producto con el nombre {}", product.getName());
            throw new ConflictException("Ya existe un producto con el nombre " + product.getName());
        }

        product.setActive(true);
        ProductEntity saved = productRepository.save(productEntityMapper.toEntity(product));
        log.info("Producto creado con id {}", saved.getId());
        return productEntityMapper.toModel(saved);
    }
}