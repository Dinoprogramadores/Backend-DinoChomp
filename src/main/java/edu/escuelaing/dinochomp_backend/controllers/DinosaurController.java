package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.services.DinosaurService;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.DinosaurMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dinosaurs")
@RequiredArgsConstructor
public class DinosaurController {

    private final DinosaurService dinosaurService;
    private final DinosaurMapper dinosaurMapper;

    @PostMapping
    public ResponseEntity<DinosaurResponseDTO> create(@RequestBody DinosaurRequestDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        Dinosaur entity = dinosaurMapper.toEntity(dto);
        Dinosaur created = dinosaurService.createDinosaur(entity);
        return new ResponseEntity<>(dinosaurMapper.toDTO(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DinosaurResponseDTO>> getAll() {
        List<Dinosaur> list = dinosaurService.getAllDinosaurs();
        return ResponseEntity.ok(list.stream().map(dinosaurMapper::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DinosaurResponseDTO> getById(@PathVariable String id) {
        return dinosaurService.getDinosaurById(id)
                .map(d -> ResponseEntity.ok(dinosaurMapper.toDTO(d)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DinosaurResponseDTO> update(@PathVariable String id, @RequestBody DinosaurRequestDTO dto) {
        Dinosaur updatedEntity = dinosaurMapper.toEntity(dto);
        return dinosaurService.updateDinosaur(id, updatedEntity)
                .map(d -> ResponseEntity.ok(dinosaurMapper.toDTO(d)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = dinosaurService.deleteDinosaur(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}