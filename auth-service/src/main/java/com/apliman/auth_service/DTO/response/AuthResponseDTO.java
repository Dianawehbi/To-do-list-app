package com.apliman.auth_service.DTO.response;

import lombok.Data;

@Data
public class AuthResponseDTO {

    private String accessToken;
    private String refreshToken;
    private long accessTokenExpiresAt;
}
