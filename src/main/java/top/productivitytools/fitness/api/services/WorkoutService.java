package top.productivitytools.fitness.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.productivitytools.fitness.api.entities.FitnessUser;
import top.productivitytools.fitness.api.entities.Workout;
import top.productivitytools.fitness.api.repositories.FitnessUserRepository;
import top.productivitytools.fitness.api.repositories.WorkoutRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    private final WorkoutRepository repository;
    private final FitnessUserRepository userRepository;

    public List<Workout> getAllWorkouts() {
        return repository.findAllByOrderByStartTimeDesc();
    }

    public List<Workout> getWorkoutsByUserId(Long userId) {
        return repository.findByUserIdOrderByStartTimeDesc(userId);
    }

    public Optional<Workout> getWorkoutById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public Workout save(Workout workout) {
        if (workout.getUser() == null || workout.getUser().getId() == null) {
            FitnessUser defaultUser = getOrCreateDefaultUser();
            workout.setUser(defaultUser);
        } else if (workout.getUser().getId() != null) {
            FitnessUser existingUser = userRepository.findById(workout.getUser().getId())
                    .orElseGet(this::getOrCreateDefaultUser);
            workout.setUser(existingUser);
        }
        return repository.save(workout);
    }

    private FitnessUser getOrCreateDefaultUser() {
        return userRepository.findByEmail("default@fitness.top")
                .orElseGet(() -> {
                    FitnessUser newUser = new FitnessUser();
                    newUser.setEmail("default@fitness.top");
                    newUser.setUsername("Default User");
                    newUser.setDefaultRestTimerSeconds(90);
                    return userRepository.save(newUser);
                });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
