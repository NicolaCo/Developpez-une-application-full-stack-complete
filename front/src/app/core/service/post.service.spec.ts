import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PostService } from './post.service';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { Post } from '../model/post.interface';

describe('PostService', () => {
  let postService: PostService;
  let httpMock: HttpTestingController;

  const post: Post = {
    id: 1,
    title: 'titleOne',
    content: 'contentOne',
    date: '2026-01-15T10:00:00Z',
    author: 'authorOne',
    topicId: 10
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClientTesting(),
        PostService
      ]
    });
    postService = TestBed.inject(PostService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(postService).toBeTruthy();
  });

  it('should call the list endpoint with the default desc order', () => {
    postService.all().subscribe(posts => {
      expect(posts).toEqual([post]);
    });

    const req = httpMock.expectOne(request =>
      request.url === 'http://localhost:8080/api/posts' && request.params.get('order') === 'desc');
    expect(req.request.method).toBe('GET');
    req.flush([post]);
  });

  it('should call the list endpoint with the given asc order', () => {
    postService.all('asc').subscribe();

    const req = httpMock.expectOne(request =>
      request.url === 'http://localhost:8080/api/posts' && request.params.get('order') === 'asc');
    expect(req.request.method).toBe('GET');
    req.flush([post]);
  });

  it('should call the detail endpoint', () => {
    postService.detail(1).subscribe(result => {
      expect(result).toEqual(post);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/posts/1');
    expect(req.request.method).toBe('GET');
    req.flush(post);
  });

  it('should post the create request body', () => {
    const request = { title: 'titleOne', content: 'contentOne', topicId: 10 };

    postService.create(request).subscribe(result => {
      expect(result).toEqual(post);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/posts');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(post);
  });
});
