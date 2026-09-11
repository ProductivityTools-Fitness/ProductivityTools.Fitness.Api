package top.productivitytools.fitness.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.productivitytools.fitness.api.entities.FitnessUser;
import top.productivitytools.fitness.api.services.WorkoutService;

@RestController
@RequestMapping({"/api/user", "/user"})
@RequiredArgsConstructor
public class UserController {

    private final WorkoutService workoutService;

    @GetMapping({"/me", "/current"})
    public FitnessUser getCurrentUser() {
        return workoutService.getCurrentUser();
    }
}
