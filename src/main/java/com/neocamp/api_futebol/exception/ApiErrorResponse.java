package com.neocamp.api_futebol.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
    int status,
    String error,
    String message,
    LocalDateTime timestamp,
    List<ApiValidationError> errors
) {}
