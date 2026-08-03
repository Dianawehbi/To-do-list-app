package com.apliman.auth_service.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.apliman.auth_service.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    // @Autowired
    // private JwtService JwtService;
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        var hasBearerToken = authHeader != null && authHeader.startsWith(BEARER_PREFIX);
        if (!hasBearerToken) {
            filterChain.doFilter(request, response);
            return;
        }

        var alreadyAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null;
        if (alreadyAuthenticated) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // var principal = JwtService.validateToken(token);
        // if (principal == null) {
        //     // Deliberately continues unauthenticated: rejecting here would bypass the
        //     // entry point and lose the JSON error body configured in ConfigSecurity.
        //     log.debug("Token rejected by auth service for {}", request.getRequestURI());
        //     filterChain.doFilter(request, response);
        //     return;
        // }
        // var authentication = new UsernamePasswordAuthenticationToken(
        //         principal, null, ((AbstractAuthenticationToken) principal).getAuthorities());
        // authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // SecurityContextHolder.getContext().setAuthentication(authentication);
        // filterChain.doFilter(request, response);
        try {
            var username = jwtService.extractUserName(token);
            var userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.validateToken(token, userDetails)) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.debug("Token rejected for {}", request.getRequestURI());
            }
        } catch (Exception e) {
            // Deliberately continues unauthenticated: rejecting here would bypass the
            // entry point and lose the JSON error body configured in ConfigSecurity.
            log.debug("Token validation failed for {}: {}", request.getRequestURI(), e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}
