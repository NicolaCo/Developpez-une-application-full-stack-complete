package com.openclassrooms.mddapi.authentication.exception;

public class UsernameAlreadyUsedException extends RuntimeException {

    public UsernameAlreadyUsedException() {
        super("Username already used");
    }
}
