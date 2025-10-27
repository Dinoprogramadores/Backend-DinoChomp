package edu.escuelaing.dinochomp_backend.services;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.escuelaing.dinochomp_backend.model.Dinosaur;
import edu.escuelaing.dinochomp_backend.repository.DinosaurRepository;

@Service
public class DinosaurService {
    @Autowired
    private DinosaurRepository dinosaurRepository;

    public Dinosaur createDinosaur(Dinosaur dinosaur) {
        return dinosaurRepository.save(dinosaur);
    }
    
    public Optional<Dinosaur> getDinosaurById(String id) {
        return dinosaurRepository.findById(id);
    }

    public boolean deleteDinosaur(String id) {
        if (dinosaurRepository.existsById(id)) {
            dinosaurRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Dinosaur> getAllDinosaurs() {
        return dinosaurRepository.findAll();
    }
    public Optional<Dinosaur> updateDinosaur(String id, Dinosaur updatedDinosaur) {
        return dinosaurRepository.findById(id).map(existingDinosaur -> {
            existingDinosaur.setName(updatedDinosaur.getName());
            existingDinosaur.setDamage(updatedDinosaur.getDamage());
            return dinosaurRepository.save(existingDinosaur);
        });
    }

    
}
