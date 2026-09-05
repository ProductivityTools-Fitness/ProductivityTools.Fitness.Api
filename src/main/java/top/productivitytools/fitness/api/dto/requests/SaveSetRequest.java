package top.productivitytools.fitness.api.dto.requests;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public record SaveSetRequest(
    @JsonAlias({"id", "setId"})
    Long id,

    @JsonAlias({"kg", "weightKg", "weight"})
    BigDecimal kg,

    Integer reps,

    @JsonAlias({"status", "isCompleted", "completed"})
    Boolean status
) {}
