package com.dmApp.dto;

public record CreateTaskRequestDto(
        Long userId,
        String title,
        String description
) {
}
