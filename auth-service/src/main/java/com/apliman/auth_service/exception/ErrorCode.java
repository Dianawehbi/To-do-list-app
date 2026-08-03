package com.apliman.auth_service.exception;

public enum ErrorCode {
    UNAUTHORIZED,
    FORBIDDEN,
    VALIDATION_ERROR,
    NOT_FOUND,
    DUPLICATE_RESOURCE,
    SELF_ACTION_NOT_ALLOWED,
    INTERNAL_ERROR
}