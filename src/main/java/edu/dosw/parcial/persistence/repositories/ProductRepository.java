package edu.dosw.parcial.persistence.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import edu.dosw.parcial.persistence.entities.ProductEntity;

public interface ProductRepository extends MongoRepository<ProductEntity, String> {

    boolean existsByNameIgnoreCase(String name);
}