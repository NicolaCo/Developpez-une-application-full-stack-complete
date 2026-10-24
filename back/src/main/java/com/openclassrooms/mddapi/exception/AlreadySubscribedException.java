package com.openclassrooms.mddapi.exception;

public class AlreadySubscribedException extends RuntimeException {

    public AlreadySubscribedException() {
        super("Already subscribed to this topic");
    }
}
