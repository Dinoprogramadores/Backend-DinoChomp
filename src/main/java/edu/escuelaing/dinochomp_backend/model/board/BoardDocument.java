package edu.escuelaing.dinochomp_backend.model.board;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@Document(collection = "Boards")
public class BoardDocument {

    @Id
    private String id;

    private int width;
    private int height;

    // Mapa serializable para Mongo, las claves son "x,y"
    private Map<String, Object> cells = new HashMap<>();
}