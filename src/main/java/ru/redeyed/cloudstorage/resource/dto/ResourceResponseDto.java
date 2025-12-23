package ru.redeyed.cloudstorage.resource.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import ru.redeyed.cloudstorage.resource.ResourceType;

public record ResourceResponseDto(

        String path,

        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Long size,

        ResourceType type) {
}
