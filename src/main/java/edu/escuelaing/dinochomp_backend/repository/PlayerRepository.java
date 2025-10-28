package edu.escuelaing.dinochomp_backend.repository;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends MongoRepository<Player,String> {
    
}
