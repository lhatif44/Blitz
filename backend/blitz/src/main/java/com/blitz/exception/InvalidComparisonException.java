package com.blitz.exception;

public class InvalidComparisonException extends RuntimeException{
    public InvalidComparisonException(String message){
        super(message);
    }
}