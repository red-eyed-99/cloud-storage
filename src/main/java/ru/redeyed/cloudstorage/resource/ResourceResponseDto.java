package ru.redeyed.cloudstorage.resource;

public record ResourceResponseDto(String path, String name, Long size, ResourceType type) {
}
