package edu.escuelaing.dinochomp_backend.repository;

import edu.escuelaing.dinochomp_backend.model.power.Power;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerRepository extends MongoRepository<Power, String> {
}
