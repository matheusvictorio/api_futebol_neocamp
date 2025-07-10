package com.neocamp.api_futebol.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
