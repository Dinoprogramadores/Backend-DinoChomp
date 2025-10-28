package edu.escuelaing.dinochomp_backend.model.board;

import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import org.springframework.data.annotation.Id;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;

import java.awt.Point;
import java.io.Serializable;
import java.util.*;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Boards")
public class Board implements Serializable {

    @Id
    private String id;
    private int width;
    private int height;
    private Map<Point, Object> map;
    private boolean isComplete = false;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new HashMap<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map.put(new Point(x, y), null);
            }
        }
    }

    public Object getItem(Point p) {
        return map.get(p);
    }

    public void setItem(Point p, Object obj) {
        if (insideBoard(p)) {
            map.put(p, obj);
        }
    }

    public boolean isNull(Point p) {
        return insideBoard(p) && map.get(p) == null;
    }

    private boolean insideBoard(Point p) {
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }

    public void addPlayer(Player player) {
        Point p = new Point(player.getPositionX(), player.getPositionY());
        if (isNull(p)) {
            map.put(p, player);
        } else {
            throw new IllegalStateException("It is not possible to place the player in " + p);
        }
    }

    public void addFood(Food food) {
        Point p = new Point(food.getPositionX(), food.getPositionY());
        if (isNull(p)) {
            map.put(p, food);
        } else {
            throw new IllegalStateException("It is not possible to place the food in " + p);
        }
    }

    public void movePlayer(Player player, int newX, int newY) {
        Point current = new Point(player.getPositionX(), player.getPositionY());
        Point destination = new Point(newX, newY);

        if (!insideBoard(destination)) return;
        Object contenidoDestino = map.get(destination);

        if (contenidoDestino == null || contenidoDestino instanceof Food) {
            map.put(current, null);
            map.put(destination, player);
            player.setPositionX(newX);
            player.setPositionY(newY);
            act(destination, player);
        }
    }

    private void act(Point p, Player player) {
        Object obj = map.get(p);
        if (obj instanceof Food food) {
            map.put(p, null);
            player.addHealth(food.getNutritionValue());
        }
    }

}

