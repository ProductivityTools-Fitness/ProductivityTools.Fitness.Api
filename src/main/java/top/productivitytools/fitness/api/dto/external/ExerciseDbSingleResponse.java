package top.productivitytools.fitness.api.dto.external;

public record ExerciseDbSingleResponse(
    boolean success,
    ExerciseDbItem data
) {}
