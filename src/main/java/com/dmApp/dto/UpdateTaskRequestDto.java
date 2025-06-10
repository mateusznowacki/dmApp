package com.dmApp.dto;

import com.dmApp.enums.TaskStatus;

import java.time.LocalDateTime;

public record UpdateTaskRequestDto(
        String title,
        String description,
        boolean completed,
        LocalDateTime finishedAt,
        TaskStatus status
) {
}
