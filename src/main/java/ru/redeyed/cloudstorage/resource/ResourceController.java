package ru.redeyed.cloudstorage.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.redeyed.cloudstorage.auth.UserDetailsImpl;
import ru.redeyed.cloudstorage.common.http.ContentDispositionType;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.resource.dto.ResourceResponseDto;
import ru.redeyed.cloudstorage.resource.validation.annotation.ValidResourceFiles;
import ru.redeyed.cloudstorage.resource.validation.annotation.ValidResourcePath;
import ru.redeyed.cloudstorage.resource.validation.annotation.ValidSearchQuery;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping("/resource")
    public ResponseEntity<ResourceResponseDto> get(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @RequestParam @ValidResourcePath String path) {

        var resourceResponseDto = resourceService.getResource(userDetails.getId(), path);
        return ResponseEntity.ok(resourceResponseDto);
    }

    @PostMapping("/resource")
    public ResponseEntity<List<ResourceResponseDto>> uploadFiles(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = PathUtil.PATH_DELIMITER) @ValidResourcePath(onlyDirectory = true) String path,
            @RequestPart @ValidResourceFiles List<MultipartFile> files
    ) {
        var resourceResponseDtos = resourceService.uploadFiles(userDetails.getId(), path, files);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceResponseDtos);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<StreamingResponseBody> download(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @RequestParam @ValidResourcePath String path) {

        var streamingResponseBody = resourceService.downloadResource(userDetails.getId(), path);

        var contentDispositionType = ContentDispositionType.ATTACHMENT.getValue();

        var contentDisposition = ContentDisposition.builder(contentDispositionType)
                .filename(PathUtil.extractResourceName(path))
                .build();

        var headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(contentDisposition);

        return ResponseEntity.ok()
                .headers(headers)
                .body(streamingResponseBody);
    }

    @GetMapping("/resource/move")
    public ResponseEntity<ResourceResponseDto> move(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                    @RequestParam @ValidResourcePath String from,
                                                    @RequestParam @ValidResourcePath String to) {

        var resourceResponseDto = resourceService.moveResource(userDetails.getId(), from, to);
        return ResponseEntity.ok(resourceResponseDto);
    }

    @GetMapping("/resource/search")
    public ResponseEntity<List<ResourceResponseDto>> search(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @RequestParam @ValidSearchQuery String query) {

        var resourceResponseDtos = resourceService.search(userDetails.getId(), query);
        return ResponseEntity.ok(resourceResponseDtos);
    }

    @DeleteMapping("/resource")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetailsImpl userDetails,
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
