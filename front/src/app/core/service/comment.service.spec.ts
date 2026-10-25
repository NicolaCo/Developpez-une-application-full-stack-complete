import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CommentService } from './comment.service';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { Comment } from '../model/comment.interface';

describe('CommentService', () => {
  let commentService: CommentService;
  let httpMock: HttpTestingController;

  const comment: Comment = {
    id: 1,
    content: 'First comment',
    date: '2026-01-15T11:00:00Z',
    author: 'authorOne',
    postId: 10
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClientTesting(),
        CommentService
      ]
    });
    commentService = TestBed.inject(CommentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(commentService).toBeTruthy();
  });

  it('should call the list endpoint for a given post', () => {
    commentService.all(10).subscribe(comments => {
      expect(comments).toEqual([comment]);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/posts/10/comments');
    expect(req.request.method).toBe('GET');
    req.flush([comment]);
  });

  it('should post the create request body for a given post', () => {
    const request = { content: 'First comment' };

    commentService.create(10, request).subscribe(result => {
      expect(result).toEqual(comment);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/posts/10/comments');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(comment);
  });
});
