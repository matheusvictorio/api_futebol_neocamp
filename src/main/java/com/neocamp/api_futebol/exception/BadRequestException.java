package com.neocamp.api_futebol.exception;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message);
    }
}
