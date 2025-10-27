package edu.escuelaing.dinochomp_backend.repository;

import edu.escuelaing.dinochomp_backend.model.Power;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerRepository extends JpaRepository<Power, String> {
}
