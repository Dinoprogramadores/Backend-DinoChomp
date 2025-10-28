package edu.escuelaing.dinochomp_backend.controller;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.BoardService;
import edu.escuelaing.dinochomp_backend.utils.dto.board.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody CreateBoardRequestDTO dto) {
        Board board = boardService.createBoard(dto.getWidth(), dto.getHeight());
        return ResponseEntity.ok(board);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<Board> getBoard(@PathVariable String boardId) {
        return boardService.getBoard(boardId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{boardId}/players")
    public ResponseEntity<Board> addPlayer(@PathVariable String boardId, @RequestBody AddPlayerDTO dto) {
        Player player = new Player();
        player.setId(dto.getPlayerId());
        player.setPositionX(dto.getPositionX());
        player.setPositionY(dto.getPositionY());
        Board updated = boardService.addPlayer(boardId, player);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{boardId}/foods")
    public ResponseEntity<Board> addFood(@PathVariable String boardId, @RequestBody AddFoodDTO dto) {
        Food food = new Food();
        food.setId(dto.getFoodId());
        food.setPositionX(dto.getPositionX());
        food.setPositionY(dto.getPositionY());
        food.setNutritionValue(dto.getNutritionValue());
        Board updated = boardService.addFood(boardId, food);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{boardId}/players/move")
    public ResponseEntity<Board> movePlayer(@PathVariable String boardId, @RequestBody MovePlayerDTO dto) {
        // ⚠️ En un caso real, deberías recuperar al jugador del servicio o la BD
        Player player = new Player();
        player.setId(dto.getPlayerId());
        Board updated = boardService.movePlayer(boardId, player, dto.getNewX(), dto.getNewY());
        return ResponseEntity.ok(updated);
    }
}