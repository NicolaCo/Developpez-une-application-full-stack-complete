package com.openclassrooms.mddapi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.topic.TopicResponse;
import com.openclassrooms.mddapi.service.TopicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final TopicService topicService;

    @GetMapping
    public List<TopicResponse> list() {
        return topicService.list();
    }

    @PostMapping("/{id}/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@PathVariable Long id) {
        topicService.subscribe(id);
    }

    @DeleteMapping("/{id}/subscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable Long id) {
        topicService.unsubscribe(id);
    }
}
