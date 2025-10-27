package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.Power;
import edu.escuelaing.dinochomp_backend.services.PowerService;
import edu.escuelaing.dinochomp_backend.utils.dto.PowerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.PowerResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.PowerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/powers")
public class PowerController {

    @Autowired
    private PowerService powerService;

    @Autowired
    private PowerMapper powerMapper;

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
        return ResponseEntity.ok(list.stream().map(powerMapper::toDTO).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PowerResponseDTO> getById(@PathVariable String name) {
        return powerService.getPowerByName(name)
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