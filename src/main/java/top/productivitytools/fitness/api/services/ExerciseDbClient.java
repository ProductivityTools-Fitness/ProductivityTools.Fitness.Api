package top.productivitytools.fitness.api.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import top.productivitytools.fitness.api.dto.external.ExerciseDbItem;
import top.productivitytools.fitness.api.dto.external.ExerciseDbSearchResponse;
import top.productivitytools.fitness.api.dto.external.ExerciseDbSingleResponse;

import java.util.List;
import java.util.Optional;

@Component
public class ExerciseDbClient {

    private final RestClient restClient;

    public ExerciseDbClient() {
        this.restClient = RestClient.builder()
                .baseUrl("https://oss.exercisedb.dev/api/v1")
                .defaultHeader("User-Agent", "ProductivityTools-Fitness-Api")
                .build();
    }

    public List<ExerciseDbItem> searchExercises(String name, String bodyParts, String equipments, Integer limit) {
        int effectiveLimit = (limit != null && limit > 0) ? Math.min(limit, 25) : 10;
        ExerciseDbSearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/exercises")
                        .queryParamIfPresent("name", Optional.ofNullable(name).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("bodyParts", Optional.ofNullable(bodyParts).filter(s -> !s.isBlank()))
                        .queryParamIfPresent("equipments", Optional.ofNullable(equipments).filter(s -> !s.isBlank()))
                        .queryParam("limit", effectiveLimit)
                        .build())
                .retrieve()
                .body(ExerciseDbSearchResponse.class);

        return response != null && response.data() != null ? response.data() : List.of();
    }

    public Optional<ExerciseDbItem> getExerciseById(String exerciseId) {
        if (exerciseId == null || exerciseId.isBlank()) {
            return Optional.empty();
        }
        ExerciseDbSingleResponse response = restClient.get()
                .uri("/exercises/{exerciseId}", exerciseId)
                .retrieve()
                .body(ExerciseDbSingleResponse.class);

        return response != null && response.data() != null ? Optional.of(response.data()) : Optional.empty();
    }
}
