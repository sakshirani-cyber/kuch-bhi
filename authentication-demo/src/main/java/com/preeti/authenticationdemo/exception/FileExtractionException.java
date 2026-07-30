package com.preeti.authenticationdemo.exception;

public class FileExtractionException extends RuntimeException {
    public FileExtractionException(String message) {
        super(message);
    }

    public FileExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
