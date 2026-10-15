package edu.dosw.parcial.controller.mappers;

import org.springframework.stereotype.Component;
import edu.dosw.parcial.controller.dtos.request.CreateProductRequest;
import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.core.models.Product;

@Component
public class ProductMapper {

    public Product toModel(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        return product;
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.isActive());
    }
}