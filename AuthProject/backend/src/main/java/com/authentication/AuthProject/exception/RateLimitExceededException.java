package com.authentication.AuthProject.exception;

public class RateLimitExceededException extends RuntimeException{

    public RateLimitExceededException(String message){
        super(message);
    }
}
