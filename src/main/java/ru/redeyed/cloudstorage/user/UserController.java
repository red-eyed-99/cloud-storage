package ru.redeyed.cloudstorage.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.redeyed.cloudstorage.user.dto.UserResponseDto;

@RestController
@RequestMapping("/api/user")
public class UserController implements UserApi {

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        var userResponseDto = new UserResponseDto(userDetails.getUsername());
        return ResponseEntity.ok(userResponseDto);
    }
}
