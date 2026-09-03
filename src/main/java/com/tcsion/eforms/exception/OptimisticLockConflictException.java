package com.tcsion.eforms.exception;
public class OptimisticLockConflictException extends RuntimeException {
    public OptimisticLockConflictException(String message) { super(message); }
}
