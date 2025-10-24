package edu.escuelaing.dinochomp_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Player")
public class Player {
    @Id
    private String id;
    private String name;
}

