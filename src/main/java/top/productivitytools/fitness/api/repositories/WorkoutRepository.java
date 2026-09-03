package top.productivitytools.fitness.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import top.productivitytools.fitness.api.entities.Workout;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findAllByOrderByStartTimeDesc();
    List<Workout> findByUserIdOrderByStartTimeDesc(Long userId);
}
