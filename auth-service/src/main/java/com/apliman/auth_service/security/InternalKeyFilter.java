package com.apliman.auth_service.security;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class InternalKeyFilter extends OncePerRequestFilter {

    @Value("${auth.internal-key}")
    private String expectedKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals("/api/auth/introspect")) {
            String providedKey = request.getHeader("X-Internal-Key");
            if (providedKey == null || !providedKey.equals(expectedKey)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Invalid or missing internal key\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}