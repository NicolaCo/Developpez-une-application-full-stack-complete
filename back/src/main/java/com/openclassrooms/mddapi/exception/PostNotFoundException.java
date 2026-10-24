package com.openclassrooms.mddapi.exception;

public class PostNotFoundException extends ResourceNotFoundException {

    public PostNotFoundException(Long id) {
        super("Post not found: " + id);
    }
}
