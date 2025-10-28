package edu.escuelaing.dinochomp_backend.repository;

import edu.escuelaing.dinochomp_backend.model.food.Food;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodRepository extends MongoRepository<Food,String> {
    
}
