package com.apliman.task_service.security;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import com.apliman.task_service.DTO.response.IntrospectResponseDTO;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenIntrospectionFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate;

    @Value("${auth.internal-key}")
    private String internalKey;

    @Value("${auth-service.base-url}")
    private String authServiceBaseUrl;

    public TokenIntrospectionFilter(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(2))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = authHeader.substring(7).trim();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Key", internalKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            var body = Map.of("token", token);
            var entity = new HttpEntity<>(body, headers);

            var result = restTemplate.postForObject(
                    authServiceBaseUrl + "/api/auth/introspect", entity, IntrospectResponseDTO.class);

            if (result != null && result.isActive()) {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + result.getRole()));
                var authentication = new UsernamePasswordAuthenticationToken(
                        result.getUsername(), null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // active == false -> proceed unauthenticated, entry point handles the 401

        } catch (ResourceAccessException e) {
            // auth-service unreachable or timed out -> 503, not 401
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Auth service unavailable\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}