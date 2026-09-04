package top.productivitytools.fitness.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import top.productivitytools.fitness.api.dto.requests.AddExercisesRequest;
import top.productivitytools.fitness.api.entities.Exercise;
import top.productivitytools.fitness.api.entities.FitnessUser;
import top.productivitytools.fitness.api.entities.Workout;
import top.productivitytools.fitness.api.entities.WorkoutExercise;
import top.productivitytools.fitness.api.entities.WorkoutSet;
import top.productivitytools.fitness.api.repositories.ExerciseRepository;
import top.productivitytools.fitness.api.repositories.FitnessUserRepository;
import top.productivitytools.fitness.api.repositories.WorkoutExerciseRepository;
import top.productivitytools.fitness.api.repositories.WorkoutRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    private final WorkoutRepository repository;
    private final FitnessUserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;

    public List<Workout> getAllWorkouts() {
        return repository.findAllByOrderByStartTimeDesc();
    }

    public List<Workout> getWorkoutsByUserId(Long userId) {
        return repository.findByUserIdOrderByStartTimeDesc(userId);
    }

    public Optional<Workout> getWorkoutById(Long id) {
        return repository.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatusCode.NOT_FOUND,"workout not found"));
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

    @Transactional
    public Workout addExercisesToWorkout(Long workoutId, AddExercisesRequest request) {
        Workout workout = repository.findById(workoutId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found with id: " + workoutId));

        if (request == null || request.exerciseIds() == null || request.exerciseIds().isEmpty()) {
            return workout;
        }

        int nextOrderIndex = workout.getExercises().size() + 1;

        for (Long exerciseId : request.exerciseIds()) {
            Exercise exercise = exerciseRepository.findById(exerciseId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found with id: " + exerciseId));

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setOrderIndex(nextOrderIndex++);
            workoutExercise.setRestTimerSeconds(
                    workout.getUser() != null ? workout.getUser().getDefaultRestTimerSeconds() : 90
            );

            // Add an initial empty set so the user can immediately log their weight and reps
            WorkoutSet initialSet = new WorkoutSet();
            initialSet.setWorkoutExercise(workoutExercise);
            initialSet.setSetNumber(1);
            initialSet.setSetType("NORMAL");
            initialSet.setWeightKg(BigDecimal.ZERO);
            initialSet.setReps(0);
            initialSet.setIsCompleted(false);
            workoutExercise.getSets().add(initialSet);

            workoutExerciseRepository.save(workoutExercise);
            workout.getExercises().add(workoutExercise);
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
