package ru.redeyed.cloudstorage.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.redeyed.cloudstorage.common.util.DataUnit;
import ru.redeyed.cloudstorage.resource.dto.MaxFileSizeErrorResponseDto;
import ru.redeyed.cloudstorage.resource.exception.FileExtensionChangedException;
import ru.redeyed.cloudstorage.resource.exception.ResourceAlreadyExistsException;
import ru.redeyed.cloudstorage.resource.exception.ResourceNotFoundException;
import ru.redeyed.cloudstorage.user.UserAlreadyExistsException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handle(BadCredentialsException ignore) {
        return getErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
    }

    @ExceptionHandler({UserAlreadyExistsException.class, ResourceAlreadyExistsException.class})
    public ResponseEntity<ErrorResponseDto> handle(RuntimeException exception) {
        return getErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(FileExtensionChangedException.class)
    public ResponseEntity<ErrorResponseDto> handle(FileExtensionChangedException exception) {
        return getErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handle(ResourceNotFoundException exception) {
        return getErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handle(ConstraintViolationException exception) {
        var message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElseThrow();

        return getErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handle(MethodArgumentNotValidException exception) {
        var message = exception.getBindingResult()
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();

        return getErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handle(HandlerMethodValidationException exception) {
        var message = exception.getAllErrors()
                .getFirst()
                .getDefaultMessage();

        return getErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handle(HttpMessageNotReadableException ignore) {
        return getErrorResponse(HttpStatus.BAD_REQUEST, "Invalid json format.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MaxFileSizeErrorResponseDto> handle(MaxUploadSizeExceededException ignore) {
        if (!hasDataUnit(maxFileSize)) {
            maxFileSize = maxFileSize + DataUnit.BYTE.getSuffix();
        }

        var maxFileSizeValue = Long.parseLong(maxFileSize.replaceAll("\\D", ""));
        var maxFileSizeUnit = maxFileSize.replaceAll("\\d", "");

        var responseDto = new MaxFileSizeErrorResponseDto("File too large.", maxFileSizeValue, maxFileSizeUnit);

        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(responseDto);
    }

    private boolean hasDataUnit(String maxFileSize) {
        for (var dataUnit : DataUnit.values()) {
            if (maxFileSize.contains(dataUnit.getSuffix())) {
                return true;
            }
        }

        return false;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handle(NoResourceFoundException ignore) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handle(Exception exception) {
        log.error(exception.getMessage(), exception);
        return getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error.");
    }

    private ResponseEntity<ErrorResponseDto> getErrorResponse(HttpStatus status, String message) {
        var errorResponseDto = new ErrorResponseDto(message);

        return ResponseEntity
                .status(status)
                .body(errorResponseDto);
    }
}
