// package com.apliman.task_service.security;

// import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.MediaType;
// import org.springframework.http.client.SimpleClientHttpRequestFactory;
// import org.springframework.security.authentication.AbstractAuthenticationToken;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.stereotype.Component;
// import org.springframework.web.client.ResourceAccessException;
// import org.springframework.web.client.RestClient;
// import org.springframework.web.client.RestClientResponseException;

// import java.time.Duration;
// import java.time.Instant;
// import java.util.List;
// import java.util.concurrent.ConcurrentHashMap;

// /**
//  * Validates opaque bearer tokens by calling auth-service's introspection
//  * endpoint (section 7 of the project spec). task-service never parses or
//  * signs anything itself — this class is the single point where that
//  * network call happens.
//  */
// @Component
// public class TokenValidator {

//     private static final Logger log = LoggerFactory.getLogger(TokenValidator.class);
//     private static final String INTROSPECT_PATH = "/api/auth/introspect";
//     private static final Duration MAX_CACHE_TTL = Duration.ofSeconds(60);
//     private static final Duration TIMEOUT = Duration.ofSeconds(2);

//     private final RestClient restClient;
//     private final ConcurrentHashMap<String, CachedResult> cache = new ConcurrentHashMap<>();

//     public TokenValidator(@Value("${auth-service.base-url}") String authServiceBaseUrl,
//                            @Value("${auth-service.internal-key}") String internalKey) {

//         var requestFactory = new SimpleClientHttpRequestFactory();
//         requestFactory.setConnectTimeout((int) TIMEOUT.toMillis());
//         requestFactory.setReadTimeout((int) TIMEOUT.toMillis());

//         this.restClient = RestClient.builder()
//                 .baseUrl(authServiceBaseUrl)
//                 .requestFactory(requestFactory)
//                 .defaultHeader("X-Internal-Key", internalKey)
//                 .build();
//     }

//     /**
//      * Returns an authenticated principal if the token is active, or null if
//      * auth-service reached back and said it's not (expired, revoked,
//      * malformed, or belongs to a disabled account — section 7 treats all of
//      * these the same way: {"active": false}).
//      *
//      * @throws AuthServiceUnavailableException if auth-service couldn't be
//      *         reached or timed out. The caller must turn this into a 503,
//      *         not a 401 — per the spec, being unable to verify identity is
//      *         a different situation from someone being unauthorized.
//      */
//     public AbstractAuthenticationToken validate(String token) {
//         var cached = cache.get(token);
//         if (cached != null && !cached.isExpired()) {
//             return cached.authentication();
//         }

//         var response = introspect(token);

//         if (response == null || !response.active()) {
//             cache.remove(token);
//             return null;
//         }

//         var authorities = List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + response.role()));
//         var principal = new AuthPrincipal(response.userId(), response.username(), response.role());
//         var authentication = new OpaqueTokenAuthentication(principal, authorities);

//         var ttl = cacheTtlFor(response.expiresAt());
//         cache.put(token, new CachedResult(authentication, Instant.now().plus(ttl)));

//         return authentication;
//     }

//     private IntrospectionResponse introspect(String token) {
//         try {
//             return restClient.post()
//                     .uri(INTROSPECT_PATH)
//                     .contentType(MediaType.APPLICATION_JSON)
//                     .body(new IntrospectionRequest(token))
//                     .retrieve()
//                     .body(IntrospectionResponse.class);
//         } catch (ResourceAccessException ex) {
//             // Connect/read timeout, connection refused, DNS failure, etc.
//             log.warn("auth-service unreachable during introspection: {}", ex.getMessage());
//             throw new AuthServiceUnavailableException("auth-service unreachable", ex);
//         } catch (RestClientResponseException ex) {
//             // A healthy auth-service always answers introspect with HTTP 200
//             // (section 7): "Both responses come back as a normal HTTP 200."
//             // Any non-200 here means something's actually wrong with the
//             // service, not with the token — treat it the same as unreachable.
//             log.warn("auth-service returned unexpected status {} during introspection",
//                     ex.getStatusCode());
//             throw new AuthServiceUnavailableException(
//                     "auth-service returned " + ex.getStatusCode(), ex);
//         }
//     }

//     /** Cache for no longer than 60s, and never past the token's own expiry. */
//     private Duration cacheTtlFor(Instant tokenExpiresAt) {
//         if (tokenExpiresAt == null) {
//             return MAX_CACHE_TTL;
//         }
//         var untilExpiry = Duration.between(Instant.now(), tokenExpiresAt);
//         if (untilExpiry.isNegative()) {
//             return Duration.ZERO;
//         }
//         return untilExpiry.compareTo(MAX_CACHE_TTL) < 0 ? untilExpiry : MAX_CACHE_TTL;
//     }

//     // ---- request/response shapes for the introspection call ----

//     private record IntrospectionRequest(String token) {
//     }

//     @JsonIgnoreProperties(ignoreUnknown = true)
//     private record IntrospectionResponse(boolean active, Long userId, String username,
//                                           String role, Instant expiresAt) {
//     }

//     private record CachedResult(OpaqueTokenAuthentication authentication, Instant expiresAt) {
//         boolean isExpired() {
//             return Instant.now().isAfter(expiresAt);
//         }
//     }
// }