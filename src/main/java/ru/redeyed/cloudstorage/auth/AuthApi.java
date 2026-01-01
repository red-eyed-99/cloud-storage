package ru.redeyed.cloudstorage.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ru.redeyed.cloudstorage.auth.dto.AuthResponseDto;
import ru.redeyed.cloudstorage.auth.dto.SignInRequestDto;
import ru.redeyed.cloudstorage.auth.dto.SignUpRequestDto;
import ru.redeyed.cloudstorage.exception.ErrorResponseDto;

@Tag(name = "Auth API")
public interface AuthApi {

    @Operation(summary = "User registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration was successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "username": "user123"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Parameter 'username' must not be null or empty."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "User already exists."
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
    ResponseEntity<AuthResponseDto> signUp(
            @RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "username": "user123",
                                               "password": "12345"
                                             }
                                            """
                            )
                    )
            ) SignUpRequestDto signUpRequestDto,
            HttpServletRequest request, HttpServletResponse response
    );

    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "username": "user123"
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Parameter 'username' must not be null or empty."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "401", description = "Invalid username or password",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Invalid username or password."
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
    ResponseEntity<AuthResponseDto> signIn(
            @RequestBody(
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(
                                    value = """
                                            {
                                               "username": "user123",
                                               "password": "12345"
                                             }
                                            """
                            )
                    )
            ) SignInRequestDto signInRequestDto,
            HttpServletRequest request, HttpServletResponse response
    );

    @Operation(summary = "User logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful"),

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
    ResponseEntity<Void> signOut(HttpServletRequest request);
}
