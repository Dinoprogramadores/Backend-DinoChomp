package edu.escuelaing.dinochomp_backend.repository;

import edu.escuelaing.dinochomp_backend.model.game.Player;

import java.util.Optional;

import org.apache.commons.lang3.concurrent.UncheckedFuture;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends MongoRepository<Player,String> {

    Optional<Player> findById(String id);

    Optional<Player> findByEmail(String email);
}
