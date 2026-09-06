package top.productivitytools.fitness.api.dto.requests;

import com.fasterxml.jackson.annotation.JsonAlias;

public record DeleteSetRequest(
    @JsonAlias({"id", "setId"})
    Long id
) {}
