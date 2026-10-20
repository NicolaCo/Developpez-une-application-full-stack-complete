package com.openclassrooms.mddapi.topic.exception;

public class TopicNotFoundException extends RuntimeException {

    public TopicNotFoundException(Long id) {
        super("Topic not found: " + id);
    }
}
