package com.apliman.auth_service.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    //this class is used for logout , refresh token request body

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
