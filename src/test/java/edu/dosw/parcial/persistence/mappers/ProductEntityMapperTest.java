package edu.dosw.parcial.persistence.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import edu.dosw.parcial.core.models.Product;
import edu.dosw.parcial.persistence.entities.ProductEntity;

class ProductEntityMapperTest {

    private final ProductEntityMapper mapper = new ProductEntityMapper();

    @Test
    void toEntity_y_toModel_conservanLosDatos() {
        // Arrange
        Product product = new Product("PROD-001", "Sandwich", 5000, true);

        // Act
        ProductEntity entity = mapper.toEntity(product);
        Product result = mapper.toModel(entity);

        // Assert
        assertEquals(product, result);
    }
}