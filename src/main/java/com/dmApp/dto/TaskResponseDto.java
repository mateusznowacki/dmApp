package com.dmApp.dto;

import com.dmApp.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime finishedAt,
        TaskStatus status) {
}
