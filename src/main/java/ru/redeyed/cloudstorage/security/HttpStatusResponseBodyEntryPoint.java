package ru.redeyed.cloudstorage.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import ru.redeyed.cloudstorage.exception.ErrorResponseDto;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpStatusResponseBodyEntryPoint implements AuthenticationEntryPoint {

    private final HttpStatus httpStatus;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpStatusResponseBodyEntryPoint(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public void commence(@NonNull HttpServletRequest request, HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        response.setStatus(httpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        var errorResponse = new ErrorResponseDto(httpStatus.getReasonPhrase());

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
