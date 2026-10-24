package com.openclassrooms.mddapi.exception;

public class UsernameAlreadyUsedException extends RuntimeException {

    public UsernameAlreadyUsedException() {
        super("Username already used");
    }
}
