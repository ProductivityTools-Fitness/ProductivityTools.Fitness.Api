package top.productivitytools.fitness.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import top.productivitytools.fitness.api.dto.requests.AddExercisesRequest;
import top.productivitytools.fitness.api.dto.requests.AddSetRequest;
import top.productivitytools.fitness.api.dto.requests.SaveSetRequest;
import top.productivitytools.fitness.api.entities.Exercise;
import top.productivitytools.fitness.api.entities.FitnessUser;
import top.productivitytools.fitness.api.entities.Workout;
import top.productivitytools.fitness.api.entities.WorkoutExercise;
import top.productivitytools.fitness.api.entities.WorkoutSet;
import top.productivitytools.fitness.api.repositories.ExerciseRepository;
import top.productivitytools.fitness.api.repositories.FitnessUserRepository;
import top.productivitytools.fitness.api.repositories.WorkoutExerciseRepository;
import top.productivitytools.fitness.api.repositories.WorkoutRepository;
import top.productivitytools.fitness.api.repositories.WorkoutSetRepository;

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
    private final WorkoutSetRepository workoutSetRepository;

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
        boolean isNew = workout.getId() == null;
        Workout saved = repository.save(workout);
        if (isNew && (saved.getTitle() == null || saved.getTitle().isBlank() || saved.getTitle().equalsIgnoreCase("Log Workout") || saved.getTitle().equalsIgnoreCase("New workout"))) {
            saved.setTitle("Trening #" + saved.getId());
            saved = repository.save(saved);
        }
        return saved;
    }

    @Transactional
    public Workout updateTitle(Long workoutId, String title) {
        Workout workout = repository.findById(workoutId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found with id: " + workoutId));
        workout.setTitle(title);
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
            workoutExercise.addSet();

            workoutExerciseRepository.save(workoutExercise);
            workout.getExercises().add(workoutExercise);
        }

        return repository.save(workout);
    }

    @Transactional
    public Workout addSet(AddSetRequest request) {
        if (request == null || request.workoutId() == null || request.exerciseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "workoutId and exerciseId must be provided");
        }

        Workout workout = repository.findById(request.workoutId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found with id: " + request.workoutId()));

        WorkoutExercise workoutExercise = workout.getExercises().stream()
                .filter(we -> (we.getExercise() != null && request.exerciseId().equals(we.getExercise().getId()))
                        || request.exerciseId().equals(we.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise with id " + request.exerciseId() + " not found in workout " + request.workoutId()));

        WorkoutSet newSet = workoutExercise.addSet();
        workoutSetRepository.save(newSet);
        workoutExerciseRepository.save(workoutExercise);
        return repository.save(workout);
    }

    @Transactional
    public WorkoutSet saveSet(SaveSetRequest request) {
        if (request == null || request.id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Set ID must be provided in request body");
        }

        WorkoutSet workoutSet = workoutSetRepository.findById(request.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout set not found with id: " + request.id()));

        if (request.kg() != null) {
            workoutSet.setWeightKg(request.kg());
        }
        if (request.reps() != null) {
            workoutSet.setReps(request.reps());
        }
        if (request.status() != null) {
            workoutSet.setIsCompleted(request.status());
        }

        return workoutSetRepository.save(workoutSet);
    }

    @Transactional
    public boolean deleteSet(Long setId) {
        if (setId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Set ID must be provided");
        }

        WorkoutSet workoutSet = workoutSetRepository.findById(setId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout set not found with id: " + setId));

        WorkoutExercise workoutExercise = workoutSet.getWorkoutExercise();
        if (workoutExercise != null) {
            workoutExercise.getSets().removeIf(s -> setId.equals(s.getId()));
            int number = 1;
            for (WorkoutSet s : workoutExercise.getSets()) {
                s.setSetNumber(number++);
            }
            workoutExerciseRepository.save(workoutExercise);
        } else {
            workoutSetRepository.delete(workoutSet);
        }

        return true;
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
