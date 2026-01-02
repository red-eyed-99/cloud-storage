package ru.redeyed.cloudstorage.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.redeyed.cloudstorage.auth.UserDetailsImpl;
import ru.redeyed.cloudstorage.exception.ErrorResponseDto;
import ru.redeyed.cloudstorage.resource.dto.ResourceResponseDto;
import ru.redeyed.cloudstorage.resource.validation.annotation.SameResourceType;
import ru.redeyed.cloudstorage.resource.validation.annotation.ValidResourceFiles;
import ru.redeyed.cloudstorage.resource.validation.annotation.ValidResourcePath;
import ru.redeyed.cloudstorage.resource.validation.annotation.ValidSearchQuery;
import java.util.List;

@Tag(name = "Resource API")
public interface ResourceApi {

    @Operation(summary = "Obtaining information about a resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ResourceResponseDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Get file info example",
                                            value = """
                                                    {
                                                       "path": "folder1/folder2/",
                                                       "name": "file.txt",
                                                       "size": 123,
                                                       "type": "FILE"
                                                     }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Get directory info example",
                                            value = """
                                                    {
                                                       "path": "/",
                                                       "name": "folder1",
                                                       "type": "DIRECTORY"
                                                     }
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Parameter 'path' must not be null or empty."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "401", description = "User unauthorized",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Unauthorized."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "File not found."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ResourceResponseDto> get(
            UserDetailsImpl userDetails,
            @Parameter(
                    examples = {
                            @ExampleObject(
                                    name = "file.txt",
                                    description = "Get file.txt info from root directory",
                                    value = "file.txt"
                            ),
                            @ExampleObject(
                                    name = "folder1/file.txt",
                                    description = "Get file.txt info from folder1 directory",
                                    value = "folder1/file.txt"
                            ),
                            @ExampleObject(
                                    name = "folder1/",
                                    description = "Get folder1 info from root directory",
                                    value = "folder1/"
                            ),
                            @ExampleObject(
                                    name = "folder1/folder2/",
                                    description = "Get folder2 info from folder1 directory",
                                    value = "folder1/folder2/"
                            )
                    },
                    description = "full path to the resource"
            )
            @ValidResourcePath String path
    );

    @Operation(summary = "Uploading files")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Files uploaded successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ResourceResponseDto.class)),
                            examples = {
                                    @ExampleObject(
                                            name = "Upload file in root directory",
                                            value = """
                                                    [
                                                        {
                                                           "path": "/",
                                                           "name": "file.txt",
                                                           "size": 123,
                                                           "type": "FILE"
                                                         }
                                                     ]
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Upload file in folder directory",
                                            value = """
                                                    [
                                                        {
                                                           "path": "folder/",
                                                           "name": "file.txt",
                                                           "size": 123,
                                                           "type": "FILE"
                                                         }
                                                     ]
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Upload directory with file",
                                            value = """
                                                    [
                                                        {
                                                           "path": "/",
                                                           "name": "folder",
                                                           "type": "DIRECTORY"
                                                        },
                                                        {
                                                           "path": "folder/",
                                                           "name": "file.txt",
                                                           "size": 123,
                                                           "type": "FILE"
                                                         }
                                                     ]
                                                    """
                                    )
                            }
                    )
            ),

            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Required part 'files' is not present."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "401", description = "User unauthorized",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Unauthorized."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "409", description = "Resource already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "File already exists."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "413", description = "File too large",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "message": "File too large.",
                                               "maxFileSize": 3,
                                               "unit": "GB"
                                             }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<List<ResourceResponseDto>> uploadFiles(
            UserDetailsImpl userDetails,
            @Parameter(
                    example = "folder/",
                    description = "path to the folder where upload the resource"
            )
            @ValidResourcePath(onlyDirectory = true) String path,
            @ValidResourceFiles List<MultipartFile> files
    );

    @Operation(summary = "Downloading resources")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource downloaded successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),

            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Parameter 'path' must not be null or empty."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "401", description = "User unauthorized",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Unauthorized."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "File not found."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<StreamingResponseBody> download(
            UserDetailsImpl userDetails,
            @Parameter(
                    example = "folder/file.txt",
                    description = "path to the resource"
            )
            @ValidResourcePath String path
    );

    @SameResourceType
    ResponseEntity<ResourceResponseDto> move(
            UserDetailsImpl userDetails,
            @ValidResourcePath String from,
            @ValidResourcePath String to
    );

    ResponseEntity<List<ResourceResponseDto>> search(
            UserDetailsImpl userDetails,
            @ValidSearchQuery String query
    );

    @Operation(summary = "Deleting a resource")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The resource has been deleted or does not exist"),

            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Parameter 'path' must not be null or empty."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "401", description = "User unauthorized",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Unauthorized."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Internal server error."
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<Void> delete(
            UserDetailsImpl userDetails,
            @Parameter(
                    examples = {
                            @ExampleObject(
                                    name = "file.txt",
                                    description = "Get file.txt info from root directory",
                                    value = "file.txt"
                            ),
                            @ExampleObject(
                                    name = "folder1/file.txt",
                                    description = "Get file.txt info from folder1 directory",
                                    value = "folder1/file.txt"
                            ),
                            @ExampleObject(
                                    name = "folder1/",
                                    description = "Get folder1 info from root directory",
                                    value = "folder1/"
                            ),
                            @ExampleObject(
                                    name = "folder1/folder2/",
                                    description = "Get folder2 info from folder1 directory",
                                    value = "folder1/folder2/"
                            )
                    },
                    description = "full path to the resource"
            )
            @ValidResourcePath String path
    );

    ResponseEntity<List<ResourceResponseDto>> getDirectoryContent(
            UserDetailsImpl userDetails,
            @ValidResourcePath(onlyDirectory = true) String path
    );

    ResponseEntity<ResourceResponseDto> createDirectory(
            UserDetailsImpl userDetails,
            @ValidResourcePath(onlyDirectory = true) String path
    );
}
