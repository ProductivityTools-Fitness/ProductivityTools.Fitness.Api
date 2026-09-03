package top.productivitytools.fitness.api.dto.requests;

import java.util.List;

public record AddExercisesRequest(
    Long workoutId,
    List<Long> exerciseIds
) {}
