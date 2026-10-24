package com.openclassrooms.mddapi.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Instant date;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public Post(String title, String content, Instant date, Long topicId, Long userId) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.topicId = topicId;
        this.userId = userId;
    }
}
