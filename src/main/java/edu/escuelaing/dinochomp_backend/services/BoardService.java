package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.board.BoardDocument;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.BoardRepository;
import edu.escuelaing.dinochomp_backend.repository.FoodRepository;
import edu.escuelaing.dinochomp_backend.utils.mappers.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final FoodRepository foodRepository;

    public Board createBoard(int width, int height) {
        Board board = new Board(width, height);

        BoardDocument document = BoardMapper.toDocument(board);
        document = boardRepository.save(document);

        return BoardMapper.fromDocument(document);
    }

    public Optional<Board> getBoard(String boardId) {
        return boardRepository.findById(boardId)
                .map(BoardMapper::fromDocument);
    }

    public Board addPlayer(String boardId, Player player) {
        Board board = getBoard(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Point position = new Point(player.getPositionX(), player.getPositionY());
        board.getMap().put(position, player);

        BoardDocument doc = BoardMapper.toDocument(board);
        boardRepository.save(doc);

        return board;
    }

    public Board addFood(String boardId, Food food) {
        Board board = getBoard(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        Point position = new Point(food.getPositionX(), food.getPositionY());
        board.getMap().put(position, food);

        BoardDocument doc = BoardMapper.toDocument(board);
        boardRepository.save(doc);

        return board;
    }

    public Board movePlayer(String boardId, Player player, int newX, int newY) {
        Board board = getBoard(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.movePlayer(player, newX, newY);

        BoardDocument doc = BoardMapper.toDocument(board);
        boardRepository.save(doc);

        return board;
    }

}
