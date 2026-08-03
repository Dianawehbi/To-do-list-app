package com.apliman.auth_service.exception;

public class SelfActionNotAllowedException extends RuntimeException {
    public SelfActionNotAllowedException(String message) {
        super(message);
    }
}