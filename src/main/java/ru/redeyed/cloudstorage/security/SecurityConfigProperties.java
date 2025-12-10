package ru.redeyed.cloudstorage.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "security")
public record SecurityConfigProperties(

        Map<HttpMethod, String[]> openEndpoints,

        CorsConfigProperties cors
) {

    @ConfigurationProperties(prefix = "cors")
    public record CorsConfigProperties(

            String applyPattern,

            List<String> allowedOrigins,

            List<String> allowedOriginPatterns,

            List<String> allowedMethods,

            List<String> allowedHeaders,
            List<String> exposedHeaders,

            Boolean allowCredentials,

            Boolean allowPrivateNetwork,

            Long maxAge
    ) {
    }
}
