package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public Board createBoard(int width, int height) {
        Board board = new Board(width, height);
        return boardRepository.save(board);
    }

    public Optional<Board> getBoard(String id) {
        return boardRepository.findById(id);
    }

    public Board addPlayer(String boardId, Player player) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalStateException("Board not found: " + boardId));

        board.addPlayer(player);
        return boardRepository.save(board);
    }

    public Board addFood(String boardId, Food food) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalStateException("Board not found: " + boardId));

        board.addFood(food);
        return boardRepository.save(board);
    }

    public Board movePlayer(String boardId, Player player, int newX, int newY) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalStateException("Board not found: " + boardId));

        board.movePlayer(player, newX, newY);
        return boardRepository.save(board);
    }

    public void printBoard(String boardId) {
        boardRepository.findById(boardId).ifPresent(Board::imprimir);
    }

    public void clearPosition(String boardId, Point point) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalStateException("Board not found: " + boardId));
        board.setItem(point, null);
        boardRepository.save(board);
    }
}