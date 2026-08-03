package com.apliman.task_service.exception;

import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // @ExceptionHandler(ResourceNotFoundException.class)
    // public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    //     return build(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ex.getMessage(), request, null);
    // }

    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    //     Map<String, String> fieldErrors = new HashMap<>();
    //     ex.getBindingResult().getFieldErrors().forEach(error ->
    //             fieldErrors.put(error.getField(), error.getDefaultMessage())
    //     );

    //     return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Validation failed", request, fieldErrors);
    // }

    // @ExceptionHandler(DuplicateResourceException.class)
    // public ResponseEntity<ErrorResponseDTO> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
    //     return build(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_RESOURCE, ex.getMessage(), request, null);
    // }

    // @ExceptionHandler(InvalidTokenException.class)
    // public ResponseEntity<ErrorResponseDTO> handleInvalidToken(InvalidTokenException ex, HttpServletRequest request) {
    //     return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), request, null);
    // }

    // @ExceptionHandler(InvalidCredentialsException.class)
    // public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
    //     return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ex.getMessage(), request, null);
    // }

    // @ExceptionHandler(SelfActionNotAllowedException.class)
    // public ResponseEntity<ErrorResponseDTO> handleSelfAction(SelfActionNotAllowedException ex, HttpServletRequest request) {
    //     return build(HttpStatus.CONFLICT, ErrorCode.SELF_ACTION_NOT_ALLOWED, ex.getMessage(), request, null);
    // }

    // private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, ErrorCode error, String message,
    //                                                  HttpServletRequest request, Map<String, String> fieldErrors) {
    //     ErrorResponseDTO body = new ErrorResponseDTO(
    //             message,
    //             status.value(),
    //             error,
    //             request.getRequestURI(),
    //             Instant.now().toString(),
    //             fieldErrors
    //     );
    //     return ResponseEntity.status(status).body(body);
    // }
}