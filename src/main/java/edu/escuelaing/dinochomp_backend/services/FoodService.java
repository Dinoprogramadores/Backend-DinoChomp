package edu.escuelaing.dinochomp_backend.services;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.repository.FoodRepository;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;

    public List<Food> getAllFoods() {
        return foodRepository.findAll();
    }

    public Optional<Food> getFoodById(String id) {
        return foodRepository.findById(id);
    }


    public boolean deleteFood(String id) {
        if (foodRepository.existsById(id)) {
            foodRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Food> updateFood(String id, Food foodDetails) {
        return foodRepository.findById(id).map(g -> {
            g.setName(foodDetails.getName());
            g.setPositionX(foodDetails.getPositionX());
            g.setPositionY(foodDetails.getPositionY());
            g.setNutritionValue(foodDetails.getNutritionValue());
            return foodRepository.save(g);
        });
        
    }

    public Food createFood(Food food) {
        return foodRepository.save(food);
    }
    
}
