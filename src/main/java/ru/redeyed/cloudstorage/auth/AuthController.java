package ru.redeyed.cloudstorage.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redeyed.cloudstorage.auth.dto.AuthResponseDto;
import ru.redeyed.cloudstorage.auth.dto.SignInRequestDto;
import ru.redeyed.cloudstorage.auth.dto.SignUpRequestDto;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    private final SecurityContextRepository securityContextRepository;

    @Override
    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto,
                                                  HttpServletRequest request, HttpServletResponse response) {

        var authentication = authService.signUp(signUpRequestDto);

        invalidateExistingSession(request);
        updateSecurityContext(request, response, authentication);

        var authResponseDto = new AuthResponseDto(signUpRequestDto.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authResponseDto);
    }

    @Override
    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponseDto> signIn(@Valid @RequestBody SignInRequestDto signInRequestDto,
                                                  HttpServletRequest request, HttpServletResponse response) {

        var authentication = authService.signIn(signInRequestDto);

        invalidateExistingSession(request);
        updateSecurityContext(request, response, authentication);

        var authResponseDto = new AuthResponseDto(signInRequestDto.getUsername());

        return ResponseEntity.ok(authResponseDto);
    }

    @Override
    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(HttpServletRequest request) {
        invalidateExistingSession(request);
        return ResponseEntity.noContent().build();
    }

    private void invalidateExistingSession(HttpServletRequest request) {
        var session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }
    }

    private void updateSecurityContext(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) {

        var securityContext = SecurityContextHolder.getContext();

        securityContext.setAuthentication(authentication);

        securityContextRepository.saveContext(securityContext, request, response);
    }
}
