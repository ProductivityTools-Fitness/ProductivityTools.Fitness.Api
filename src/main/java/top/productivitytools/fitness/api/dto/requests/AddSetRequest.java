package top.productivitytools.fitness.api.dto.requests;

public record AddSetRequest(
    Long workoutId,
    Long exerciseId
) {}
