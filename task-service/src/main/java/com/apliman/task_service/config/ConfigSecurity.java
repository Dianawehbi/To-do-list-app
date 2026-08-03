package com.apliman.task_service.config;

import java.time.Instant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.apliman.task_service.DTO.response.ErrorResponseDTO;
import com.apliman.task_service.exception.ErrorCode;
import com.apliman.task_service.security.TokenIntrospectionFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfigSecurity {

    private static final String[] PUBLIC_PATHS = {
        "/error"
        //  no public endpoints 
    };

    private final TokenIntrospectionFilter tokenIntrospectionFilter;

    public ConfigSecurity(TokenIntrospectionFilter tokenIntrospectionFilter) {
        this.tokenIntrospectionFilter = tokenIntrospectionFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint(new ObjectMapper()))
                        .accessDeniedHandler(accessDeniedHandler(new ObjectMapper())))
                .addFilterBefore(tokenIntrospectionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint(ObjectMapper mapper) {
        return (request, response, authException) -> {
            ErrorResponseDTO body = new ErrorResponseDTO();
            body.setMessage(authException.getMessage());
            body.setPath(request.getRequestURI());
            body.setError(ErrorCode.UNAUTHORIZED);
            body.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            body.setTimestamp(Instant.now().toString());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(response.getOutputStream(), body);
        };
    }

    private org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler(ObjectMapper mapper) {
        return (request, response, accessDeniedException) -> {
            ErrorResponseDTO body = new ErrorResponseDTO();
            body.setMessage(accessDeniedException.getMessage());
            body.setPath(request.getRequestURI());
            body.setError(ErrorCode.FORBIDDEN);
            body.setStatus(HttpServletResponse.SC_FORBIDDEN);
            body.setTimestamp(Instant.now().toString());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(response.getOutputStream(), body);
        };
    }
}