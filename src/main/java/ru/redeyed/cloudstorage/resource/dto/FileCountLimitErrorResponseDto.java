package ru.redeyed.cloudstorage.resource.dto;

public record FileCountLimitErrorResponseDto(String message, long maxFilesCount) {
}
