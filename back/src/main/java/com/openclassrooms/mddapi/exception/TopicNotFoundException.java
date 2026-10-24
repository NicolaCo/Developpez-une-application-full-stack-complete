package com.openclassrooms.mddapi.exception;

public class TopicNotFoundException extends ResourceNotFoundException {

    public TopicNotFoundException(Long id) {
        super("Topic not found: " + id);
    }
}
