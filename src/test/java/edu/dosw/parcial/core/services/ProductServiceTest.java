package edu.dosw.parcial.core.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import edu.dosw.parcial.core.exceptions.ConflictException;
import edu.dosw.parcial.core.models.Product;
import edu.dosw.parcial.persistence.entities.ProductEntity;
import edu.dosw.parcial.persistence.mappers.ProductEntityMapper;
import edu.dosw.parcial.persistence.repositories.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEntityMapper productEntityMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProduct_nombreNuevo_guardaYRetornaProductoActivo() {
        // Arrange
        Product input = new Product(null, "Sandwich", 5000, false);
        ProductEntity entity = new ProductEntity("PROD-001", "Sandwich", 5000, true);
        Product saved = new Product("PROD-001", "Sandwich", 5000, true);
        when(productRepository.existsByNameIgnoreCase("Sandwich")).thenReturn(false);
        when(productEntityMapper.toEntity(input)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productEntityMapper.toModel(entity)).thenReturn(saved);

        // Act
        Product result = productService.createProduct(input);

        // Assert
        assertEquals("PROD-001", result.getId());
        assertTrue(result.isActive());
        verify(productRepository).save(entity);
    }

    @Test
    void createProduct_nombreExistente_lanzaConflictYNoGuarda() {
        // Arrange
        Product input = new Product(null, "Sandwich", 5000, false);
        when(productRepository.existsByNameIgnoreCase("Sandwich")).thenReturn(true);

        // Act
        ConflictException exception = assertThrows(ConflictException.class,
                () -> productService.createProduct(input));

        // Assert
        assertEquals("Ya existe un producto con el nombre Sandwich", exception.getMessage());
        verify(productRepository, never()).save(any());
    }
}