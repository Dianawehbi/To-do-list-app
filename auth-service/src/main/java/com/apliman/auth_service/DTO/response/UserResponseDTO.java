package com.apliman.auth_service.DTO.response;

import lombok.Data;

@Data
public class UserResponseDTO {

    private long id;
    private String username;
    private String email;
    private String role;
    private boolean enabled;
}
