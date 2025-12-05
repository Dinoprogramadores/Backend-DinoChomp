package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.PlayerService;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.PlayerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerMapper playerMapper;

    @PostMapping
    public ResponseEntity<PlayerResponseDTO> create(@RequestBody PlayerRequestDTO dto) {
        if (dto == null) return ResponseEntity.badRequest().build();
        Player entity = playerMapper.toEntity(dto);
        Player created = playerService.savePlayer(entity);
        return new ResponseEntity<>(playerMapper.toDTO(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PlayerResponseDTO>> getAll() {
        List<Player> list = playerService.getAllPlayers();
        return ResponseEntity.ok(list.stream().map(playerMapper::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> getById(@PathVariable String id) {
        return playerService.getPlayerById(id)
                .map(p -> ResponseEntity.ok(playerMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email")
    public ResponseEntity<PlayerResponseDTO> getByEmail(@RequestParam String email) {
        return playerService.getPlayerByEmail(email)
                .map(p -> ResponseEntity.ok(playerMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> update(@PathVariable String id, @RequestBody PlayerRequestDTO dto) {
        Player updatedEntity = playerMapper.toEntity(dto);
        return playerService.updatePlayer(id, updatedEntity)
                .map(p -> ResponseEntity.ok(playerMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        boolean deleted = playerService.deletePlayer(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}