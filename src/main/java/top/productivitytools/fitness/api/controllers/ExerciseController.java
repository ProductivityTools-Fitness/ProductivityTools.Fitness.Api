package top.productivitytools.fitness.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.productivitytools.fitness.api.entities.Exercise;
import top.productivitytools.fitness.api.entities.Workout;
import top.productivitytools.fitness.api.services.ExerciseService;

import java.util.List;

@RestController
@RequestMapping("/api/exercise")
@RequiredArgsConstructor
public class ExerciseController {
    
    private final ExerciseService exerciseService;

    @GetMapping("/list")
    public List<Exercise> getExerciseList() {
        return exerciseService.getExerciseList();
    }
}
