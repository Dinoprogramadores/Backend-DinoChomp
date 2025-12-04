package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.power.Power;
import edu.escuelaing.dinochomp_backend.services.PowerService;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.PowerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/powers")
@RequiredArgsConstructor
public class PowerController {

    private final PowerService powerService;
    private final PowerMapper powerMapper;

    @PostMapping
    public ResponseEntity<PowerResponseDTO> create(@RequestBody PowerRequestDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        Power entity = powerMapper.toEntity(dto);
        Power created = powerService.createPower(entity);
        return new ResponseEntity<>(powerMapper.toDTO(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PowerResponseDTO>> getAll() {
        List<Power> list = powerService.getAllPowers();
        return ResponseEntity.ok(list.stream().map(powerMapper::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PowerResponseDTO> getById(@PathVariable String id) {
        return powerService.getPowerByName(id)
                .map(p -> ResponseEntity.ok(powerMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PowerResponseDTO> update(@PathVariable String id, @RequestBody PowerRequestDTO dto) {
        Power updatedEntity = powerMapper.toEntity(dto);
        return powerService.updatePower(id, updatedEntity)
                .map(p -> ResponseEntity.ok(powerMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = powerService.deletePower(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}