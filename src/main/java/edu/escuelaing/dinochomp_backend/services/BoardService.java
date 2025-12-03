package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.board.BoardDocument;
import edu.escuelaing.dinochomp_backend.model.board.BoardItem;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.BoardRepository;
import edu.escuelaing.dinochomp_backend.repository.FoodRepository;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import edu.escuelaing.dinochomp_backend.utils.DinoChompException;
import edu.escuelaing.dinochomp_backend.utils.mappers.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final FoodRepository foodRepository;
    private final PlayerRepository playerRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String EPMTYVALUE= "EMPTY";
    private static final String FOODITEM= "FOOD:";
    private static final String PLAYERITEM= "PLAYER:";

    private String boardKey(String boardId) {
        return "board:" + boardId + ":cells";
    }

    public Board createBoard(int width, int height) {
        Board board = new Board(width, height);

        BoardDocument document = BoardMapper.toDocument(board);
        document = boardRepository.save(document);

        String key = boardKey(document.getId());
        // Inicializa todas las celdas como vacías usando "EMPTY" en lugar de null
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                redisTemplate.opsForHash().put(key, x + "," + y, EPMTYVALUE);
            }
        }

        return BoardMapper.fromDocument(document);
    }

    public Optional<Board> getBoard(String boardId) {
        return boardRepository.findById(boardId).map(doc -> {
            Board board = BoardMapper.fromDocument(doc);

            String key = boardKey(boardId);
            Map<Object, Object> redisMap = redisTemplate.opsForHash().entries(key);

            Map<Point, BoardItem> map = board.getMap();
            map.clear();

            redisMap.forEach((k, v) -> {
                String[] parts = k.toString().split(",");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                Point point = new Point(x, y);

                BoardItem item = null;

                if (v == null || EPMTYVALUE.equals(v.toString())) {
                    item = null;

                } else if (v.toString().startsWith(FOODITEM)) {
                    String foodId = v.toString().substring(5);
                    item = foodRepository.findById(foodId).orElse(null);

                } else if (v.toString().startsWith(PLAYERITEM)) {
                    String playerId = v.toString().substring(7);
                    item = playerRepository.findById(playerId).orElse(null);
                }

                map.put(point, item);
            });

            return board;
        });
    }

    public Board addPlayer(String boardId, Player player) {
        playerRepository.save(player);

        String key = boardKey(boardId);
        String cell = player.getPositionX() + "," + player.getPositionY();

        redisTemplate.opsForHash().put(key, cell, PLAYERITEM + player.getId());

        return getBoard(boardId)
                .orElseThrow(() -> new DinoChompException("Board not found"));
    }

    public Board addFood(String boardId, Food food) {
        foodRepository.save(food);

        String key = boardKey(boardId);
        String cell = food.getPositionX() + "," + food.getPositionY();

        redisTemplate.opsForHash().put(key, cell, FOODITEM + food.getId());

        return getBoard(boardId)
                .orElseThrow(() -> new DinoChompException("Board not found"));
    }

    public Optional<Food> movePlayer(String boardId, Player player, int newX, int newY) {
        String key = boardKey(boardId);

        String origin = player.getPositionX() + "," + player.getPositionY();
        String dest = newX + "," + newY;

        Object itemAtDest = redisTemplate.opsForHash().get(key, dest);
        Food eaten = null;

        // Validar que el destino no esté ocupado por otro jugador
        if (itemAtDest instanceof String id && id.startsWith(PLAYERITEM)) {
            System.out.println("Movimiento bloqueado: casilla (" + newX + "," + newY + ") ocupada");
            // No mover, solo retornar sin comida
            return Optional.empty();
        }

        // Verificar si hay comida en el destino
        if (itemAtDest instanceof String id && id.startsWith(FOODITEM)) {
            String foodId = id.replace(FOODITEM, "");
            eaten = foodRepository.findById(foodId).orElse(null);
            foodRepository.deleteById(foodId);
        }

        // Limpiar la posición origen
        redisTemplate.opsForHash().put(key, origin, EPMTYVALUE);

        // Mover al jugador a la nueva posición
        redisTemplate.opsForHash().put(key, dest, PLAYERITEM + player.getId());

        // Actualizar la posición del jugador en su entidad y guardarlo
        player.setPositionX(newX);
        player.setPositionY(newY);
        playerRepository.save(player);

        return Optional.ofNullable(eaten);
    }

}