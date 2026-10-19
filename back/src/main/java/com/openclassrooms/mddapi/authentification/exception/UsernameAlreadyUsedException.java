package com.openclassrooms.mddapi.authentification.exception;

public class UsernameAlreadyUsedException extends RuntimeException {

    public UsernameAlreadyUsedException() {
        super("Username already used");
    }
}
