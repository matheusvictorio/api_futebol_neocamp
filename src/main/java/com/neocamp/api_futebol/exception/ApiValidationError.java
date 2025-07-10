package com.neocamp.api_futebol.exception;

public record ApiValidationError(String field, String message) {}
