package com.jasonqiu98.dutchessayhelper.requests;

import jakarta.validation.constraints.NotBlank;

public record CorrectionRequest(
    @NotBlank String taskType,
    @NotBlank String prompt,
    @NotBlank String essay
) {
}
