package top.productivitytools.fitness.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.productivitytools.fitness.api.entities.Exercise;
import top.productivitytools.fitness.api.repositories.ExerciseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;

    public List<Exercise> getExerciseList() {
        return exerciseRepository.findAllByOrderByNameAsc();
    }

    public List<Exercise> getAvailableExercises(Long userId) {
        if (userId == null) {
            return exerciseRepository.findByIsSystemTrueOrderByNameAsc();
        }
        return exerciseRepository.findAvailableExercisesForUser(userId);
    }
}
