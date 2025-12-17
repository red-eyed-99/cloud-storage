package ru.redeyed.cloudstorage.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    private final ResourceMapper resourceMapper;

    public ResourceResponseDto getResource(UUID userId, String path) {
        var resourceName = ResourceUtil.extractResourceName(path);
        var resourcePath = ResourceUtil.removeResourceName(path);

        return resourceRepository.findBy(userId, resourcePath, resourceName)
                .map(resourceMapper::toResourceResponseDto)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public void deleteResource(UUID userId, String path) {
        var resourceName = ResourceUtil.extractResourceName(path);
        var resourcePath = ResourceUtil.removeResourceName(path);
        resourceRepository.deleteBy(userId, resourcePath, resourceName);
    }

    public ResourceResponseDto createDirectory(UUID userId, String path) {
        var resourceName = ResourceUtil.extractResourceName(path);
        var resourcePath = ResourceUtil.removeResourceName(path);

        if (ResourceUtil.isRootDirectory(resourcePath)) {
            return saveResource(userId, resourcePath, resourceName, ResourceType.DIRECTORY);
        }

        if (resourceExists(userId, resourcePath, ResourceType.DIRECTORY)) {
            return saveResource(userId, resourcePath, resourceName, ResourceType.DIRECTORY);
        }

        throw new ResourceNotFoundException("Parent directory not found.");
    }

    public boolean resourceExists(UUID userId, String path, ResourceType type) {
        var resourceName = ResourceUtil.extractResourceName(path);
        var resourcePath = ResourceUtil.removeResourceName(path);
        return resourceRepository.exists(userId, resourcePath, resourceName, type);
    }

    private ResourceResponseDto saveResource(UUID userId, String path, String name, ResourceType type) {
        var resource = Resource.builder()
                .path(path)
                .name(name)
                .type(type)
                .userId(userId)
                .build();

        try {
            resourceRepository.saveAndFlush(resource);
        } catch (DataIntegrityViolationException exception) {
            throw new ResourceAlreadyExistsException();
        }

        return resourceMapper.toResourceResponseDto(resource);
    }
}
