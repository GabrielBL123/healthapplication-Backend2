package com.gabrielbl.healthaplication.exception;

public class AlreadySubmittedException extends RuntimeException {
    public AlreadySubmittedException(String message, Throwable cause) {
        super(message, cause);
    }
    public AlreadySubmittedException(String message) {
        super(message);
    }
}
