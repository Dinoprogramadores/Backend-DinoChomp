package edu.escuelaing.dinochomp_backend.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.model.power.HealthPower;
import edu.escuelaing.dinochomp_backend.model.power.Power;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import edu.escuelaing.dinochomp_backend.repository.PowerRepository;
import java.util.function.Supplier;

@Service
public class PowerService {
    @Autowired
    private PowerRepository powerRepository;
    @Autowired
    private PlayerRepository playerRepository;

    // lista de poderes disponibles
    private final List<Supplier<Power>> powerPool =
    new ArrayList<>(Arrays.asList(
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
        System.out.println("poder random asignado a: "+player.getId());
        Power selectedPower = powerPool.get(random.nextInt(powerPool.size())).get();
        Player newPlayer = selectedPower.applyEffect(player);
        System.out.println("id DEL NEWPLAYER "+newPlayer.getId());
        playerRepository.save(newPlayer);
        return newPlayer;
    }
    
}