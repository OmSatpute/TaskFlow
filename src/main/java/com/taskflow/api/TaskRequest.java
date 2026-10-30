package com.taskflow.api;

import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.DashboardProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record TaskRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 2000) String description,
        @NotNull TaskPriority priority,
        @NotNull TaskStatus status,
        LocalDateTime deadline,
        Long categoryId,
        DashboardProfile profile) { }
