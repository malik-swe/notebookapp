package com.example.notebookapp.config;

import com.example.notebookapp.security.jwt.JwtAuthFilter;
import com.example.notebookapp.security.logging.SecurityEventLogger;
import com.example.notebookapp.security.ratelimit.RateLimitFilter;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;
    private final SecurityEventLogger securityEventLogger;

    public SecurityConfig(JwtAuthFilter jwtFilter, RateLimitFilter rateLimitFilter, SecurityEventLogger securityEventLogger) {
        this.jwtFilter = jwtFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.securityEventLogger = securityEventLogger;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                "/users/register",
                                "/users/login",
                                "/users/register-form"
                        ).permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // User and Admin can access notes
                        .requestMatchers("/notes/**").hasAnyRole("USER", "ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        // X-Content-Type-Options
                        .contentTypeOptions(contentType -> {})

                        // X-Frame-Options
                        .frameOptions(frame -> frame.deny())

                        // Content-Security-Policy (simple & safe)
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self'; " +
                                                "img-src 'self'; " +
                                                "object-src 'none';"
                                )
                        )

                        // Referrer-Policy
                        .referrerPolicy(referrer ->
                                referrer.policy(
                                        org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                                )
                        )

                        //HSTS
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                )

                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(securityEventLogger)
                );


        return http.build();
    }

}
