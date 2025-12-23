package ru.redeyed.cloudstorage.resource.dto;

public record MaxFileSizeErrorResponseDto(String message, long maxFileSize, String unit) {
}
