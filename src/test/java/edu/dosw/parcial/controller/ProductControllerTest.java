package edu.dosw.parcial.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import edu.dosw.parcial.controller.dtos.request.CreateProductRequest;
import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.controller.mappers.ProductMapper;
import edu.dosw.parcial.core.models.Product;
import edu.dosw.parcial.core.services.ProductService;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductController productController;

    @Test
    void createProduct_solicitudValida_retorna201ConElProducto() {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Sandwich");
        request.setPrice(5000L);
        Product model = new Product(null, "Sandwich", 5000, false);
        Product created = new Product("PROD-001", "Sandwich", 5000, true);
        ProductResponse response = new ProductResponse("PROD-001", "Sandwich", 5000, true);
        when(productMapper.toModel(request)).thenReturn(model);
        when(productService.createProduct(model)).thenReturn(created);
        when(productMapper.toResponse(created)).thenReturn(response);

        // Act
        ResponseEntity<ProductResponse> result = productController.createProduct(request);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
    }

}


