package edu.dosw.parcial.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import edu.dosw.parcial.controller.dtos.request.CreateProductRequest;
import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.controller.mappers.ProductMapper;
import edu.dosw.parcial.core.models.Product;
import edu.dosw.parcial.core.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Productos")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crear un producto (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El rol no tiene permisos"),
            @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese nombre")
    })
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Solicitud para crear un producto");
        Product created = productService.createProduct(productMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(productMapper.toResponse(created));
    }
}