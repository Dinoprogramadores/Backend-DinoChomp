package edu.escuelaing.dinochomp_backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.escuelaing.dinochomp_backend.model.Player;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    public Optional<Player> getPlayerById(String id) {
        return playerRepository.findById(id);

    }

    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    public boolean deletePlayer(String id) {
        if (playerRepository.existsById(id)) {
            playerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Player> updatePlayer(String id, Player updatedPlayer) {
        return playerRepository.findById(id).map(player -> {
            player.setName(updatedPlayer.getName());
            player.setPassword(updatedPlayer.getPassword());
            player.setPositionX(updatedPlayer.getPositionX());
            player.setPositionY(updatedPlayer.getPositionY());
            player.setHealth(updatedPlayer.getHealth());
            player.setAlive(updatedPlayer.isAlive());
            return playerRepository.save(player);
        });
    }

    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

}
