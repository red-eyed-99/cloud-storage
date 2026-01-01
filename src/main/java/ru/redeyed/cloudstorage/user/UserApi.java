package ru.redeyed.cloudstorage.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import ru.redeyed.cloudstorage.exception.ErrorResponseDto;
import ru.redeyed.cloudstorage.user.dto.UserResponseDto;

@Tag(name = "User API")
public interface UserApi {

    @Operation(summary = "Get the username of authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "username": "user123"
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
    ResponseEntity<UserResponseDto> getCurrentUser(UserDetails userDetails);
}
