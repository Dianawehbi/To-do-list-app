package com.apliman.auth_service.DTO.response;

import java.util.Map;

import com.apliman.auth_service.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private String message;
    private int status;
    private ErrorCode error;
    private String path;
    private String timestamp;
    private Map<String, String> fieldErrors;
}

// `fieldErrors` only appears when it's a validation failure.
// {
//   "timestamp": "2026-07-27T10:15:30Z",
//   "status": 400,
//   "error": "VALIDATION_ERROR",
//   "message": "Request validation failed",
//   "path": "/api/tasks",
//   "fieldErrors": {
//     "title": "must not be blank"
//   }
// }

