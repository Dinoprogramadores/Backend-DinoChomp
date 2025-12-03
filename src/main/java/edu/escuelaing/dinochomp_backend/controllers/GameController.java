package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameMapper gameMapper;

    @PostMapping
    public ResponseEntity<GameResponseDTO> createGame(@RequestBody GameRequestDTO gameRequest) {
        if (gameRequest.getNombre() == null || gameRequest.getNombre().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Game entity = gameMapper.toEntity(gameRequest);
        Game created = gameService.createGame(entity, gameRequest.getTotalFood());
        return new ResponseEntity<>(gameMapper.toDTO(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        List<GameResponseDTO> dtos = games.stream().map(gameMapper::toDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getGameById(@PathVariable String id) {
        return gameService.getGameById(id)
                .map(g -> ResponseEntity.ok(gameMapper.toDTO(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/board")
    public ResponseEntity<Map<String, String>> getBoardIdByGame(@PathVariable String id) {
        return gameService.getGameById(id)
                .map(game -> {
                    if (game.getBoardId() != null) {
                        return ResponseEntity.ok(Map.of("boardId", game.getBoardId()));
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", "El juego no tiene un tablero asociado"));
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Juego no encontrado")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDTO> updateGame(@PathVariable String id, @RequestBody GameRequestDTO gameRequest) {
        Game updatedEntity = gameMapper.toEntity(gameRequest);
        return gameService.updateGame(id, updatedEntity)
                .map(g -> ResponseEntity.ok(gameMapper.toDTO(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable String id) {
        boolean deleted = gameService.deleteGame(id);
        if (deleted) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/players/{playerId}")
    public ResponseEntity<GameResponseDTO> addPlayerDinosaur(@PathVariable String id, @PathVariable String playerId, @RequestBody DinosaurRequestDTO dinosaurDTO) {
        Dinosaur d = gameMapper.toEntity(dinosaurDTO);
        return gameService.addPlayerDinosaur(id, playerId, d)
                .map(g -> ResponseEntity.ok(gameMapper.toDTO(g)))
                .orElse(new ResponseEntity<>(HttpStatus.CONFLICT));
    }

    @PostMapping("/{id}/timer/start")
    public ResponseEntity<GameResponseDTO> startTimer(@PathVariable String id, @RequestParam int minutes) {
        return gameService.startTimer(id, minutes)
                .map(g -> ResponseEntity.ok(gameMapper.toDTO(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/timer/stop")
    public ResponseEntity<GameResponseDTO> stopTimer(@PathVariable String id) {
        return gameService.stopTimer(id)
                .map(g -> ResponseEntity.ok(gameMapper.toDTO(g)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/timer/remaining")
    public ResponseEntity<Long> getRemaining(@PathVariable String id) {
        return gameService.getRemainingSeconds(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/winner")
    public ResponseEntity<Player> getWinner(@PathVariable String id) {
        return gameService.getWinner(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/winner/compute")
    public ResponseEntity<Player> computeWinner(@PathVariable String id) {
        return gameService.computeAndSetWinner(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
