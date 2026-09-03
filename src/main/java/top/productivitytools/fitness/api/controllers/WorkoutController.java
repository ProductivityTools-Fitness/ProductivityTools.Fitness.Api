package top.productivitytools.fitness.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

        public Workout addExerciseToWorkout(
        @PathVariable Long workoutId,
        @RequestBody AddExercisesRequest request){
            return workoutService.addExercisesToWorkout(workoutId,request)
        }
}
