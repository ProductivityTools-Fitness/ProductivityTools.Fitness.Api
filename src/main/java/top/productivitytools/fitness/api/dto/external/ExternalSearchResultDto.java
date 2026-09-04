package top.productivitytools.fitness.api.dto.external;

import java.util.List;

public record ExternalSearchResultDto(
    String externalExerciseId,
    String name,
    String gifUrl,
    String bodyCategory,
    String equipmentCategory,
    String targetMuscle,
    List<String> secondaryMuscles,
    List<String> instructions,
    boolean isAlreadyImported,
    Long localExerciseId
) {}
