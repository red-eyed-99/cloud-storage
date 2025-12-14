package ru.redeyed.cloudstorage.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityConfigProperties securityConfigProperties;

    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var corsConfiguration = new CorsConfiguration();

        var corsConfigProperties = securityConfigProperties.cors();

        corsConfiguration.setAllowedOrigins(corsConfigProperties.allowedOrigins());
        corsConfiguration.setAllowedMethods(corsConfigProperties.allowedMethods());
        corsConfiguration.setAllowedHeaders(corsConfigProperties.allowedHeaders());
        corsConfiguration.setAllowCredentials(corsConfigProperties.allowCredentials());

        var corsConfigurationSource = new UrlBasedCorsConfigurationSource();

        corsConfigurationSource.registerCorsConfiguration(corsConfigProperties.applyPattern(), corsConfiguration);

        return corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> corsConfigurationSource());

        configureExceptionHandling(http);
        authorizeHttpRequests(http);

        return http.build();
    }

    private void authorizeHttpRequests(HttpSecurity httpSecurity) {
        var openEndpoints = securityConfigProperties.openEndpoints();

        httpSecurity.authorizeHttpRequests(auth -> {
            openEndpoints.forEach(
                    (method, endpoints) -> auth.requestMatchers(method, endpoints).permitAll()
            );

            auth.anyRequest().authenticated();
        });
    }

    private void configureExceptionHandling(HttpSecurity httpSecurity) {
        var httpStatusEntryPoint = new HttpStatusResponseBodyEntryPoint(HttpStatus.UNAUTHORIZED);
        httpSecurity.exceptionHandling(handling -> handling.authenticationEntryPoint(httpStatusEntryPoint));
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
