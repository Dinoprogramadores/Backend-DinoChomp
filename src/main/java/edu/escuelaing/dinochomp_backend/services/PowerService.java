package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.model.power.HealthPower;
import edu.escuelaing.dinochomp_backend.model.power.Power;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import edu.escuelaing.dinochomp_backend.repository.PowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PowerService {

    private final PowerRepository powerRepository;
    private final PlayerRepository playerRepository;

    // Lista de poderes disponibles
    private final List<Supplier<Power>> powerPool =
    new ArrayList<>(List.of(
            () -> new HealthPower(20)
    ));

    private final Random random = new Random();

    public List<Power> getAllPowers() {
        return powerRepository.findAll();
    }
    
    public Optional<Power> getPowerByName(String name) {
        return powerRepository.findById(name);
    }

    public Power savePower(Power power) {
        return powerRepository.save(power);
    }

    public boolean deletePower(String name) {
        if (powerRepository.existsById(name)) {
            powerRepository.deleteById(name);
            return true;
        }
        return false;
    }

    public Optional<Power> updatePower(String name, Power powerDetails) {
        return powerRepository.findById(name).map(player -> {
            player.setName(powerDetails.getName());
            return powerRepository.save(player);
        });
    }
    public Power createPower(Power power) {
        return powerRepository.save(power);
    }

    // Activar un poder aleatorio para un jugador
    public Player activateRandomPower(Player player) {
        Power selectedPower = powerPool.get(random.nextInt(powerPool.size())).get();
        Player newPlayer = selectedPower.applyEffect(player);
        playerRepository.save(newPlayer);
        return newPlayer;
    }
    
}