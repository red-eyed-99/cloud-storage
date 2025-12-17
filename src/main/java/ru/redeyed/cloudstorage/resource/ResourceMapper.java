package ru.redeyed.cloudstorage.resource;

import org.mapstruct.Mapper;

@Mapper
public interface ResourceMapper {

    ResourceResponseDto toResourceResponseDto(Resource resource);
}
