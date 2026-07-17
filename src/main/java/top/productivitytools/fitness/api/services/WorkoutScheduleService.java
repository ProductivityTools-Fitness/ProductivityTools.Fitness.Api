package top.productivitytools.fitness.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.productivitytools.fitness.api.entities.WorkoutSchedule;
import top.productivitytools.fitness.api.repositories.WorkoutScheduleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutScheduleService {
    private final WorkoutScheduleRepository repository;

    public List<WorkoutSchedule> getFullSchedule() {
        return repository.findAllByOrderByDayNumberAsc();
    }

    public WorkoutSchedule save(WorkoutSchedule schedule) {
        return repository.save(schedule);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
