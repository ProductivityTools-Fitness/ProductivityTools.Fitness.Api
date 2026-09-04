package top.productivitytools.fitness.api.dto.external;

import java.util.List;

public record ExerciseDbSearchResponse(
    boolean success,
    List<ExerciseDbItem> data
) {}
