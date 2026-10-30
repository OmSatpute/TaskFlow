package com.taskflow.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 60) String domain,
        @NotBlank @Size(max = 80) String name) { }
