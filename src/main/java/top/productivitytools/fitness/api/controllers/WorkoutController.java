package top.productivitytools.fitness.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import top.productivitytools.fitness.api.entities.Workout;
import top.productivitytools.fitness.api.services.WorkoutService;
import top.productivitytools.fitness.api.dto.requests.AddExercisesRequest;

import java.util.List;

@RestController
@RequestMapping({"/api/workout", "/workout"})
@RequiredArgsConstructor
public class WorkoutController {
    
    private final WorkoutService workoutService;

    @GetMapping
    public List<Workout> getAllWorkouts() {
        return workoutService.getAllWorkouts();
    }

    @PostMapping({"/add", ""})
    public Workout addWorkout(@RequestBody Workout workout) {
        return workoutService.save(workout);
    }

    @PostMapping({"/exercise", "/exercises", "/{workoutId}/exercise", "/{workoutId}/exercises"})
    public Workout addExerciseToWorkout(
            @PathVariable(required = false) Long workoutId,
            @RequestBody AddExercisesRequest request) {
        Long targetWorkoutId = (workoutId != null) ? workoutId : (request != null ? request.workoutId() : null);
        if (targetWorkoutId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workout ID must be provided in the URL path or in the request body");
        }
        return workoutService.addExercisesToWorkout(targetWorkoutId, request);
    }
}
