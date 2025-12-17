package ru.redeyed.cloudstorage.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserDetailsImpl implements UserDetails {

    private final UUID id;

    private final String username;

    private String password;

    @Override
    public @NonNull String getUsername() {
        return username;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    public void erasePassword() {
        password = null;
    }
}
