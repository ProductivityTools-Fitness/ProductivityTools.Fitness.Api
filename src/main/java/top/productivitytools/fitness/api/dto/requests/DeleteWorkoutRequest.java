package top.productivitytools.fitness.api.dto.requests;

import com.fasterxml.jackson.annotation.JsonAlias;

public record DeleteWorkoutRequest(
    @JsonAlias({"id", "workoutId"})
    Long id
) {
    public Long workoutId() {
        return id;
    }
}
