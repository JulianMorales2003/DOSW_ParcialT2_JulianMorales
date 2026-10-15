package edu.dosw.parcial.persistence.mappers;

import org.springframework.stereotype.Component;
import edu.dosw.parcial.core.models.Product;
import edu.dosw.parcial.persistence.entities.ProductEntity;

@Component
public class ProductEntityMapper {

    public ProductEntity toEntity(Product product) {
        return new ProductEntity(product.getId(), product.getName(), product.getPrice(), product.isActive());
    }

    public Product toModel(ProductEntity entity) {
        return new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.isActive());
    }
}