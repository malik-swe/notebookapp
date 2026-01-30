package com.example.notebookapp.security.logging;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventLogger implements AuthenticationEntryPoint {

    private static final Logger log =
            LoggerFactory.getLogger(SecurityEventLogger.class);

    @Override
    public void commence(
            HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            AuthenticationException authException
    ) {
        log.warn("Unauthorized access attempt: method={} path={}",
                request.getMethod(),
                request.getRequestURI());
    }
}
