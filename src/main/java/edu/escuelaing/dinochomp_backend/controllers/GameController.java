package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.utils.dto.game.AddPlayerDinosaurGame;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.GameMapper;
import edu.escuelaing.dinochomp_backend.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Game created = gameService.createGame(entity);
        return new ResponseEntity<>(gameMapper.toDTO(created), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GameResponseDTO>> getAllGames() {
        List<Game> games = gameService.getAllGames();
        List<GameResponseDTO> dtos = games.stream().map(gameMapper::toDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponseDTO> getGameById(@PathVariable String id) {
        return gameService.getGameById(id)
                .map(g -> ResponseEntity.ok(gameMapper.toDTO(g)))
                .orElse(ResponseEntity.notFound().build());
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

    @PostMapping("/addPlayer")
    public ResponseEntity<GameResponseDTO> addPlayerDinosaur(@RequestBody AddPlayerDinosaurGame addPlayerDinosaurGame) {
        return gameService.addPlayerDinosaur(addPlayerDinosaurGame)
                .map(g -> ResponseEntity.ok(gameMapper.toDTO(g)))
                .orElse(new ResponseEntity<>(HttpStatus.CONFLICT));
    }

    @DeleteMapping("/{gameId}/players/{playerName}")
    public ResponseEntity<Void> removePlayerFromMapByName(@PathVariable String gameId, @PathVariable String playerName) {
        boolean removed = gameService.removePlayerFromGameByName(gameId, playerName);
        if (removed) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
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

    @GetMapping("/{id}/players/dinosaurs")
    public ResponseEntity<Map<String, String>> getPlayerDinosaurMap(@PathVariable String id) {
        return gameService.getPlayerDinosaurMap(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/winner")
    public ResponseEntity<Player> getWinner(@PathVariable String id) {
        return gameService.getWinner(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/winner/compute")
    public ResponseEntity<Player> computeWinner(@PathVariable String id) {
        return gameService.computeAndSetWinner(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
