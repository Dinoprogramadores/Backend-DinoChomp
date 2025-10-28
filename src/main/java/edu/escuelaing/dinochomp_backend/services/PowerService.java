package edu.escuelaing.dinochomp_backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.escuelaing.dinochomp_backend.model.power.Power;
import edu.escuelaing.dinochomp_backend.repository.PowerRepository;

@Service
public class PowerService {
    @Autowired
    private PowerRepository powerRepository;

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
            player.setDuration(powerDetails.getDuration());
            player.setType(powerDetails.getType());
            return powerRepository.save(player);
        });
    }
    public Power createPower(Power power) {
        return powerRepository.save(power);
    }
    
}