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
import top.productivitytools.fitness.api.entities.WorkoutSchedule;
import top.productivitytools.fitness.api.services.WorkoutScheduleService;

import java.util.List;

@RestController
@RequestMapping("/workout-schedule")
@RequiredArgsConstructor
public class WorkoutScheduleController {
    
    private final WorkoutScheduleService service;

    @GetMapping
    public List<WorkoutSchedule> getSchedule() {
        return service.getFullSchedule();
    }

    @PostMapping
    public WorkoutSchedule addSchedule(@RequestBody WorkoutSchedule schedule) {
        return service.save(schedule);
    }

    @PutMapping("/{id}")
    public WorkoutSchedule updateSchedule(@PathVariable Long id, @RequestBody WorkoutSchedule schedule) {
        schedule.setId(id);
        return service.save(schedule);
    }

    @DeleteMapping("/{id}")
    public void deleteSchedule(@PathVariable Long id) {
        service.delete(id);
    }
}
