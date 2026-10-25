import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TopicService } from './topic.service';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { Topic } from '../model/topic.interface';

describe('TopicService', () => {
  let topicService: TopicService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClientTesting(),
        TopicService
      ]
    });
    topicService = TestBed.inject(TopicService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  })

  it('should be created', () => {
    expect(topicService).toBeTruthy();
  });

it('should call endpoint', () => {
  const topicOne: Topic = {id: 1, title: "titleOne", description: "descriptionOne", isSubscribed: false }; 
  const topicTwo: Topic = {id: 2, title: "titleTwo", description: "descriptionTwo", isSubscribed: true }; 
  const topicList = {topicOne, topicTwo};

  topicService.all().subscribe(topics => {
    expect(topics).toEqual(topicList);
  });

  const req = httpMock.expectOne('http://localhost:8080/api/topics');
  expect(req.request.method).toBe('GET');
  req.flush(topicList);
})

});
