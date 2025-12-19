package ru.redeyed.cloudstorage.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.redeyed.cloudstorage.auth.UserDetailsImpl;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidResourcePath;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/resource")
    public ResponseEntity<ResourceResponseDto> getResource(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                           @RequestParam @ValidResourcePath String path) {

        var resourceResponseDto = resourceService.getResource(userDetails.getId(), path);
        return ResponseEntity.ok(resourceResponseDto);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<Void> deleteResource(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @RequestParam @ValidResourcePath String path) {

        resourceService.deleteResource(userDetails.getId(), path);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/directory")
    public ResponseEntity<List<ResourceResponseDto>> getDirectoryContent(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                         @RequestParam(defaultValue = PathUtil.PATH_DELIMITER)
                                                                         @ValidResourcePath(onlyDirectory = true) String path) {

        var resourceResponseDtos = resourceService.getDirectoryContent(userDetails.getId(), path);
        return ResponseEntity.ok(resourceResponseDtos);
    }

    @PostMapping("/directory")
    public ResponseEntity<ResourceResponseDto> createDirectory(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                               @RequestParam @ValidResourcePath(onlyDirectory = true)
                                                               String path) {

        var resourceResponseDto = resourceService.createDirectory(userDetails.getId(), path);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceResponseDto);
    }
}
