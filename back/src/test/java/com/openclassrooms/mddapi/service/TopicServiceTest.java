package com.openclassrooms.mddapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.openclassrooms.mddapi.security.SecurityCurrentUserProvider;
import com.openclassrooms.mddapi.dto.topic.TopicResponse;
import com.openclassrooms.mddapi.exception.AlreadySubscribedException;
import com.openclassrooms.mddapi.exception.TopicNotFoundException;
import com.openclassrooms.mddapi.model.Subscription;
import com.openclassrooms.mddapi.model.Topic;
import com.openclassrooms.mddapi.repository.SubscriptionRepository;
import com.openclassrooms.mddapi.repository.TopicRepository;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {

    private static final long USER_ID = 1L;
    private static final long SUBSCRIBED_TOPIC_ID = 10L;
    private static final long OTHER_TOPIC_ID = 20L;
    private static final long UNKNOWN_TOPIC_ID = 9999L;
    private static final String TOPIC_ONE_DESCRIPTION = "Framework front-end TypeScript.";
    private static final String TOPIC_TWO_DESCRIPTION = "Framework Java for REST APIs.";

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SecurityCurrentUserProvider currentUserProvider;

    private TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = new TopicService(topicRepository, subscriptionRepository, currentUserProvider);
    }

    @Test
    void list_whenNoSubscription_returnsAllTopicsNotSubscribed() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(subscriptionRepository.findAllByUserId(USER_ID)).thenReturn(List.of());
        when(topicRepository.findAll(any(Sort.class))).thenReturn(List.of(
                new Topic(SUBSCRIBED_TOPIC_ID, "Angular", TOPIC_ONE_DESCRIPTION),
                new Topic(OTHER_TOPIC_ID, "Spring Boot", TOPIC_TWO_DESCRIPTION)));

        List<TopicResponse> topics = topicService.list();

        assertThat(topics).containsExactly(
                new TopicResponse(SUBSCRIBED_TOPIC_ID, "Angular", TOPIC_ONE_DESCRIPTION, false),
                new TopicResponse(OTHER_TOPIC_ID, "Spring Boot", TOPIC_TWO_DESCRIPTION, false));
    }

    @Test
    void list_whenSubscribed_returnsSubscribedTopicFlagged() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(subscriptionRepository.findAllByUserId(USER_ID))
                .thenReturn(List.of(new Subscription(USER_ID, SUBSCRIBED_TOPIC_ID)));
        when(topicRepository.findAll(any(Sort.class))).thenReturn(List.of(
                new Topic(SUBSCRIBED_TOPIC_ID, "Angular", TOPIC_ONE_DESCRIPTION),
                new Topic(OTHER_TOPIC_ID, "Spring Boot", TOPIC_TWO_DESCRIPTION)));

        List<TopicResponse> topics = topicService.list();

        assertThat(topics).extracting(TopicResponse::isSubscribed).containsExactly(true, false);
    }

    @Test
    void subscribe_whenTopicMissing_throwsAndDoesNotSave() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(topicRepository.existsById(UNKNOWN_TOPIC_ID)).thenReturn(false);

        assertThatThrownBy(() -> topicService.subscribe(UNKNOWN_TOPIC_ID))
                .isInstanceOf(TopicNotFoundException.class);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_whenAlreadySubscribed_throwsAndDoesNotSave() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(topicRepository.existsById(SUBSCRIBED_TOPIC_ID)).thenReturn(true);
        when(subscriptionRepository.findByUserIdAndTopicId(USER_ID, SUBSCRIBED_TOPIC_ID))
                .thenReturn(Optional.of(new Subscription(USER_ID, SUBSCRIBED_TOPIC_ID)));

        assertThatThrownBy(() -> topicService.subscribe(SUBSCRIBED_TOPIC_ID))
                .isInstanceOf(AlreadySubscribedException.class);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_whenValid_savesSubscriptionForCurrentUser() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(topicRepository.existsById(SUBSCRIBED_TOPIC_ID)).thenReturn(true);
        when(subscriptionRepository.findByUserIdAndTopicId(USER_ID, SUBSCRIBED_TOPIC_ID))
                .thenReturn(Optional.empty());

        topicService.subscribe(SUBSCRIBED_TOPIC_ID);

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getTopicId()).isEqualTo(SUBSCRIBED_TOPIC_ID);
    }

    @Test
    void unsubscribe_whenTopicMissing_throwsAndDoesNotDelete() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(topicRepository.existsById(UNKNOWN_TOPIC_ID)).thenReturn(false);

        assertThatThrownBy(() -> topicService.unsubscribe(UNKNOWN_TOPIC_ID))
                .isInstanceOf(TopicNotFoundException.class);
        verify(subscriptionRepository, never()).deleteByUserIdAndTopicId(any(), any());
    }

    @Test
    void unsubscribe_whenValid_deletesSubscription() {
        when(currentUserProvider.currentUserId()).thenReturn(USER_ID);
        when(topicRepository.existsById(SUBSCRIBED_TOPIC_ID)).thenReturn(true);

        topicService.unsubscribe(SUBSCRIBED_TOPIC_ID);

        verify(subscriptionRepository).deleteByUserIdAndTopicId(eq(USER_ID), eq(SUBSCRIBED_TOPIC_ID));
    }
}
