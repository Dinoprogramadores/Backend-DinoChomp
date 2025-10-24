package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.Game;
import edu.escuelaing.dinochomp_backend.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    public Game createGame(Game game) {
        // nombre es el id
        return gameRepository.save(game);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> getGameById(String id) {
        return gameRepository.findById(id);
    }

    public Optional<Game> updateGame(String id, Game updated) {
        return gameRepository.findById(id).map(g -> {
            g.setActive(updated.isActive());
            g.setPlayerDinosaurMap(updated.getPlayerDinosaurMap());
            g.setPowers(updated.getPowers());
            g.setMetadata(updated.getMetadata());
            g.setDurationMinutes(updated.getDurationMinutes());
            g.setStartTime(updated.getStartTime());
            g.setTimerActive(updated.isTimerActive());
            return gameRepository.save(g);
        });
    }

    public boolean deleteGame(String id) {
        if (gameRepository.existsById(id)) {
            gameRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Game> addPlayerDinosaur(String gameId, String playerId, Dinosaur dinosaur) {
        return gameRepository.findById(gameId).flatMap(g -> {
            boolean ok = g.addPlayerDinosaur(playerId, dinosaur);
            if (!ok) {
                return Optional.empty();
            }
            return Optional.of(gameRepository.save(g));
        });
    }

    public Optional<Game> startTimer(String gameId, int minutes) {
        return gameRepository.findById(gameId).map(g -> {
            g.setDurationMinutes(minutes);
            g.setStartTime(Instant.now());
            g.setTimerActive(true);
            return gameRepository.save(g);
        });
    }

    public Optional<Game> stopTimer(String gameId) {
        return gameRepository.findById(gameId).map(g -> {
            g.setTimerActive(false);
            return gameRepository.save(g);
        });
    }

    public Optional<Long> getRemainingSeconds(String gameId) {
        return gameRepository.findById(gameId).map(g -> {
            g.refreshTimerState();
            return g.getRemainingSeconds();
        });
    }
}
