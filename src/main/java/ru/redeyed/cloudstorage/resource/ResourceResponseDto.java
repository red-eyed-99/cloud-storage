package ru.redeyed.cloudstorage.resource;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ResourceResponseDto(

        String path,

        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long size,

        ResourceType type) {
}
