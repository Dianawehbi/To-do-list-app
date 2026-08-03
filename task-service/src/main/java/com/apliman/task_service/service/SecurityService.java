package com.apliman.task_service.service;

public class SecurityService {

    public void validateToken(String token){

    // verification call
    // `POST http://auth-service:8081/api/auth/introspect`

    // Request:
    // ```json
    // { "token": "<raw token from the Authorization header>" }
    // ```

    // If the token is valid, the response looks like this:
    // ```json
    // {
    //   "active": true,
    //   "userId": 42,
    //   "username": "jdoe",
    //   "role": "USER",
    //   "expiresAt": "2026-07-27T10:30:00Z"
    // }
    // ```

    // If it's expired, malformed, revoked, or belongs to a disabled account:
    // ```json
    // { "active": false }
    // ```
    }
 
}
