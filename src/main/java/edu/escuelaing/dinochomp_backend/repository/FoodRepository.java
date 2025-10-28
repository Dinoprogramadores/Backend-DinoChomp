package edu.escuelaing.dinochomp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;


import edu.escuelaing.dinochomp_backend.model.food.Food;

public interface FoodRepository extends JpaRepository<Food,String> {
    
}
