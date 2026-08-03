package com.apliman.auth_service.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IntrospectRequestDTO {

    @NotBlank(message = "Token is required")
    private String token;
}