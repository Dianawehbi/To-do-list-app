package com.apliman.auth_service.DTO.response;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntrospectResponseDTO {

    private boolean active;
    private Long userId;
    private String username;
    private String role;
    private Instant expiresAt;

    // convenience factory for the "inactive" case
    public static IntrospectResponseDTO inactive() {
        return new IntrospectResponseDTO(false, null, null, null, null);
    }
}