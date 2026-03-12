package com.fsse2510.fsse2510_project_backend.security;
 
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable @PreAuthorize support
@Slf4j
public class SecurityConfig {
    @Value("${app.admin.emails:admin@user.com}")
    private String adminEmails;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public API: Anyone can view Product API
                        .requestMatchers("/public/**").permitAll()
                        // Stripe Webhooks (No Auth, verifies signature)
                        .requestMatchers("/webhooks/**").permitAll()

                        // Other APIs: Require Login (Authenticated)
                        // Specific Admin permissions (Create/Update/Delete) are handled at the
                        // Controller level via @PreAuthorize
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Method A: Grant ROLE_ADMIN by checking Email
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            log.debug("--- JWT Auth Checking ---");
            
            // Check for the custom claim "admin"
            Boolean isAdmin = (Boolean) jwt.getClaims().get("admin");
            log.debug("Is Admin Claim present & true? {}", isAdmin);

            if (Boolean.TRUE.equals(isAdmin)) {
                log.info("MATCHED! User has Firebase admin claim. Granting ROLE_ADMIN");
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                // Fallback check: still check if the email is on the admin list, just in case (optional!)
                String email = (String) jwt.getClaims().get("email");
                List<String> adminList = Arrays.asList(adminEmails.split(","));
                
                if (email != null && adminList.contains(email.trim())) {
                    log.info("No 'admin' claim found, but email is in the admin list. Granting ROLE_ADMIN as fallback.");
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    log.debug("NOT MATCHED. email={}, adminClaim={}", email, isAdmin);
                }
            }

            return authorities;
        });

        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Split frontendUrl and trim spaces to prevent CORS failure due to trailing
        // spaces in environment variables
        List<String> allowedOrigins = Arrays.stream(frontendUrl.split(","))
                .map(String::trim)
                .toList();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}