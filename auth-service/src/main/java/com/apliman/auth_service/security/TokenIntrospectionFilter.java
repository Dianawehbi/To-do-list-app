// package com.apliman.auth_service.security;

// import org.springframework.stereotype.Component;
// import org.springframework.web.client.RestTemplate;
// import org.springframework.web.filter.OncePerRequestFilter;

// import jakarta.servlet.http.HttpServletRequest;

// import java.io.IOException;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// public class TokenIntrospectionFilter extends OncePerRequestFilter {

//     private final RestTemplate restTemplate = null; // or WebClient
//     private final Cache<String, IntrospectResponse> tokenCache; // 60s cache

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//             FilterChain filterChain) throws ServletException, IOException {

//         var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//             filterChain.doFilter(request, response);
//             return;
//         }

//         var token = authHeader.substring(7).trim();

//         // Check cache first — avoid re-verifying the same token repeatedly
//         IntrospectResponse cached = tokenCache.getIfPresent(token);
//         if (cached != null) {
//             applyAuthentication(cached);
//             filterChain.doFilter(request, response);
//             return;
//         }

//         try {
//             IntrospectResponse result = callIntrospectEndpoint(token); // the "interceptor" call
//             if (result.isActive()) {
//                 tokenCache.put(token, result);
//                 applyAuthentication(result);
//             }
//         } catch (ResourceAccessException e) {
//             // auth-service unreachable/timeout → 503, not 401
//             response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
//             return;
//         }

//         filterChain.doFilter(request, response);
//     }
// }
