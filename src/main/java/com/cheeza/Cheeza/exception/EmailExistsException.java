package com.cheeza.Cheeza.exception;



public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String email) {
        super("Email '" + email + "' is already registered");
    }
}
