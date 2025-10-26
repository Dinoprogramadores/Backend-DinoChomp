package edu.escuelaing.dinochomp_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.escuelaing.dinochomp_backend.model.Player;

public interface PlayerRepository extends JpaRepository<Player,String>{
    
}
