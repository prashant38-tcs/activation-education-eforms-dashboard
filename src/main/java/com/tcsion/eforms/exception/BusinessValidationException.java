package com.tcsion.eforms.exception;
import java.util.Collections;
import java.util.List;

public class BusinessValidationException extends RuntimeException {
    private final List<String> errors;
    public BusinessValidationException(String message) {
        super(message);
        this.errors = Collections.singletonList(message);
    }
    public BusinessValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = errors;
    }
    public List<String> getErrors() { return errors; }
}
