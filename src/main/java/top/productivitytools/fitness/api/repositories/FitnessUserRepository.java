package top.productivitytools.fitness.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.productivitytools.fitness.api.entities.FitnessUser;

import java.util.Optional;

@Repository
public interface FitnessUserRepository extends JpaRepository<FitnessUser, Long> {
    Optional<FitnessUser> findByEmail(String email);
}
