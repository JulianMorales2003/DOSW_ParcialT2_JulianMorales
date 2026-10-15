package edu.dosw.parcial.controller.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import edu.dosw.parcial.controller.dtos.request.CreateProductRequest;
import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.core.models.Product;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    @Test
    void toModel_copiaNombreYPrecio() {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Sandwich");
        request.setPrice(5000L);

        // Act
        Product product = mapper.toModel(request);

        // Assert
        assertEquals("Sandwich", product.getName());
        assertEquals(5000, product.getPrice());
    }

    @Test
    void toResponse_copiaLosDatosDelProducto() {
        // Arrange
        Product product = new Product("PROD-001", "Sandwich", 5000, true);

        // Act
        ProductResponse response = mapper.toResponse(product);

        // Assert
        assertEquals("PROD-001", response.getId());
    }
}