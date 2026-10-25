import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { PostDetailComponent } from './post-detail.component';
import { PostService } from '../../core/service/post.service';
import { CommentService } from '../../core/service/comment.service';
import { Post } from '../../core/model/post.interface';
import { Comment } from '../../core/model/comment.interface';

describe('PostDetailComponent', () => {
  let component: PostDetailComponent;
  let fixture: ComponentFixture<PostDetailComponent>;
  const postServiceMock = { detail: vi.fn() };
  const commentServiceMock = { all: vi.fn(), create: vi.fn() };

  const post: Post = {
    id: 1, title: 'AwsomeOne', content: 'Content One', date: '2026-01-15T10:00:00Z', author: 'authorOne', topicId: 10
  };
  const commentList: Comment[] = [
    { id: 1, content: 'First comment', date: '2026-01-15T11:00:00Z', author: 'authorOne', postId: 1 }
  ];

  beforeEach(async () => {
    postServiceMock.detail.mockReturnValue(of(post));
    commentServiceMock.all.mockReturnValue(of(commentList));
    commentServiceMock.create.mockReturnValue(of(commentList[0]));

    await TestBed.configureTestingModule({
      imports: [PostDetailComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of(convertToParamMap({ id: '1' })) }
        },
        { provide: PostService, useValue: postServiceMock },
        { provide: CommentService, useValue: commentServiceMock }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(PostDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load the post from the route id', () => {
    expect(postServiceMock.detail).toHaveBeenCalledWith(1);
  });

  it('should display the post content and author', () => {
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('AwsomeOne');
    expect(text).toContain('Content One');
    expect(text).toContain('authorOne');
  });

  describe('comments', () => {
    it('should load the comments of the post', () => {
      expect(commentServiceMock.all).toHaveBeenCalledWith(1);
      expect(fixture.nativeElement.textContent).toContain('First comment');
    });

    it('should post the comment and reload on submit', () => {
      commentServiceMock.all.mockClear();
      component['form'].setValue({ content: 'New comment' });

      component['onSubmit']();

      expect(commentServiceMock.create).toHaveBeenCalledWith(1, { content: 'New comment' });
      expect(commentServiceMock.all).toHaveBeenCalledWith(1);
    });

    it('should set onError when the creation fails', () => {
      commentServiceMock.create.mockReturnValue(throwError(() => new Error('Bad Request')));
      component['form'].setValue({ content: 'New comment' });

      component['onSubmit']();

      expect(component['onError']).toBe(true);
    });
  });
});
