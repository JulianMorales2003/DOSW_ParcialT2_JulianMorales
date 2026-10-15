package edu.dosw.parcial.controller.dtos.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class CreateProductRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private CreateProductRequest buildRequest(String name, Long price) {
        CreateProductRequest request = new CreateProductRequest();
        request.setName(name);
        request.setPrice(price);
        return request;
    }

    @Test
    void datosValidos_noHayViolaciones() {
        // Arrange
        CreateProductRequest request = buildRequest("Sandwich", 5000L);

        // Act
        var violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void nombreDeDosCaracteres_hayUnaViolacion() {
        // Arrange
        CreateProductRequest request = buildRequest("ab", 5000L);

        // Act
        var violations = validator.validate(request);

        // Assert
        assertEquals(1, violations.size());
    }

    @Test
    void precioCero_hayUnaViolacion() {
        // Arrange
        CreateProductRequest request = buildRequest("Sandwich", 0L);

        // Act
        var violations = validator.validate(request);

        // Assert
        assertEquals(1, violations.size());
    }
}