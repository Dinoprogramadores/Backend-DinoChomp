package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.services.FoodService;
import edu.escuelaing.dinochomp_backend.utils.dto.food.FoodRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.food.FoodResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.FoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/foods")
public class FoodController {

    @Autowired
    private FoodService foodService;

    @Autowired
    private FoodMapper foodMapper;

    @PostMapping
    public ResponseEntity<FoodResponseDTO> create(@RequestBody FoodRequestDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        Food entity = foodMapper.toEntity(dto);
        Food created = foodService.createFood(entity);
        return new ResponseEntity<>(foodMapper.toDTO(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FoodResponseDTO>> getAll() {
        List<Food> list = foodService.getAllFoods();
        return ResponseEntity.ok(list.stream().map(foodMapper::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodResponseDTO> getById(@PathVariable String id) {
        return foodService.getFoodById(id)
                .map(f -> ResponseEntity.ok(foodMapper.toDTO(f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FoodResponseDTO> update(@PathVariable String id, @RequestBody FoodRequestDTO dto) {
        Food updatedEntity = foodMapper.toEntity(dto);
        return foodService.updateFood(id, updatedEntity)
                .map(f -> ResponseEntity.ok(foodMapper.toDTO(f)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = foodService.deleteFood(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}