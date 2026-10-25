import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import { FeedComponent } from './feed.component';
import { PostService } from '../../core/service/post.service';
import { Post } from '../../core/model/post.interface';

describe('FeedComponent', () => {
  let component: FeedComponent;
  let fixture: ComponentFixture<FeedComponent>;
  const postServiceMock = { all: vi.fn() };

  const postList: Post[] = [
    { id: 1, title: 'AwsomeOne', content: 'Content One', date: '2026-01-15T10:00:00Z', author: 'authorOne', topicId: 10 },
    { id: 2, title: 'AwsomeTwo', content: 'Content Two', date: '2026-01-16T10:00:00Z', author: 'authorTwo', topicId: 20 }
  ];

  beforeEach(async () => {
    postServiceMock.all.mockReturnValue(of(postList));

    await TestBed.configureTestingModule({
      imports: [FeedComponent],
      providers: [
        { provide: PostService, useValue: postServiceMock },
        provideRouter([])
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should display the posts with their author', () => {
      const text = fixture.nativeElement.textContent as string;

      expect(text).toContain('AwsomeOne');
      expect(text).toContain('AwsomeTwo');
      expect(text).toContain('authorOne');
      expect(text).toContain('authorTwo');
    });
  });

  describe('order', () => {
    it('should query the posts with the default desc order', () => {
      expect(postServiceMock.all).toHaveBeenCalledWith('desc');
    });

    it('should query the posts with asc order after toggling', () => {
      postServiceMock.all.mockClear();

      component['toggleOrder']();

      expect(component['order']()).toBe('asc');
      expect(postServiceMock.all).toHaveBeenCalledWith('asc');
    });
  });
});
