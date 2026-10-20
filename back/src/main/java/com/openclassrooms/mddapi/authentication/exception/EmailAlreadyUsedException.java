package com.openclassrooms.mddapi.authentication.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("Email already used");
    }
}
