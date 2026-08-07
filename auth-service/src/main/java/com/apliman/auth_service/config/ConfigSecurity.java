package com.apliman.auth_service.config;

import java.time.Instant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.apliman.auth_service.DTO.response.ErrorResponseDTO;
import com.apliman.auth_service.exception.ErrorCode;
import com.apliman.auth_service.security.InternalKeyFilter;
import com.apliman.auth_service.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfigSecurity {

    private static final String[] PUBLIC_PATHS = {
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/logout",
        "/api/auth/refresh",
        "/api/auth/introspect", // protected by InternalKeyFilter instead of JWT
        "/error"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalKeyFilter internalKeyFilter;
    private final UserDetailsService userDetailsService;

    // private final AuthenticationEntryPoint unauthorizedEntryPoint;
    // private final List<String> allowedOrigins;
    public ConfigSecurity(
            UserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            InternalKeyFilter internalKeyFilter
    // AuthenticationEntryPoint unauthorizedEntryPoint
    ) {
        //                       @Value("${aida.security.allowed-origins}") String allowedOrigins) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.internalKeyFilter = internalKeyFilter;
        // this.unauthorizedEntryPoint = unauthorizedEntryPoint;
        //     this.allowedOrigins = Arrays.asList(allowedOrigins.split("\\s*,\\s*"));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedEntryPoint(new ObjectMapper()))
                .accessDeniedHandler(accessDeniedHandler(new ObjectMapper())))
                .addFilterBefore(internalKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     var config = new CorsConfiguration();
    //     config.setAllowedOrigins(allowedOrigins);
    //     config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    //     config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Internal-Key"));
    //     // Bearer tokens travel in the Authorization header, not cookies, so credentials
    //     // stay off — which is also what lets allowedOrigins stay explicit rather than "*".
    //     config.setAllowCredentials(false);
    //     config.setMaxAge(3600L);
    //     var source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", config);
    //     return source;
    // }
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

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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
