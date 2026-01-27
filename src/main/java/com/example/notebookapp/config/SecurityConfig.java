package com.example.notebookapp.config;

import com.example.notebookapp.security.LoggingAccessDeniedHandler;
import com.example.notebookapp.security.LoggingAuthEntryPoint;
import com.example.notebookapp.security.RateLimitFilter;
import com.example.notebookapp.security.jwt.JwtAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtFilter;
    private final LoggingAuthEntryPoint authEntryPoint;
    private final LoggingAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthFilter jwtFilter, LoggingAuthEntryPoint authEntryPoint,
                          LoggingAccessDeniedHandler accessDeniedHandler) {
        this.jwtFilter = jwtFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // === LOGGING ===
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // === SECURITY HEADERS ===
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts   //HTTPS
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                        .contentTypeOptions(contentType -> {}) // X-Content-Type-Options: nosniff
                        .frameOptions(frame -> frame.deny())   // X-Frame-Options: DENY
                        .referrerPolicy(referrer ->
                                referrer.policy(
                                        org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                                )
                        )
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "style-src 'self'; " +
                                                "img-src 'self';"
                                )
                        )
                )

                // === AUTHORIZATION ===
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/users/register",
                                "/users/login",
                                "/users/register-form"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // === JWT FILTER ===
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //=== RATE LIMITATION ===
//    @Bean
//    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(RateLimitFilter filter) {
//        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>();
//        bean.setFilter(filter);
//        bean.addUrlPatterns("/*");
//        bean.setOrder(1);
//        return bean;
//    }

}
