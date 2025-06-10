package com.dmApp.dto;

import com.dmApp.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponseDto(
        Long id,
        Long userId,
        String title,
        String description,
        boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime finishedAt,
        TaskStatus status) {
}
