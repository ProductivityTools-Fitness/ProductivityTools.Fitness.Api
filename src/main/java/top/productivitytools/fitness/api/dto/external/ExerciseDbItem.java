package top.productivitytools.fitness.api.dto.external;

import java.util.List;

public record ExerciseDbItem(
    String exerciseId,
    String name,
    String gifUrl,
    List<String> bodyParts,
    List<String> equipments,
    List<String> targetMuscles,
    List<String> secondaryMuscles,
    List<String> instructions
) {}
