import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { CreatepostComponent } from './createpost.component';
import { PostService } from '../../core/service/post.service';
import { TopicService } from '../../core/service/topic.service';
import { Topic } from '../../core/model/topic.interface';

describe('CreatepostComponent', () => {
  let component: CreatepostComponent;
  let fixture: ComponentFixture<CreatepostComponent>;
  const postServiceMock = { create: vi.fn() };
  const topicServiceMock = { all: vi.fn() };

  const topicList: Topic[] = [
    { id: 10, title: 'Angular', description: 'Front framework', isSubscribed: true }
  ];

  beforeEach(async () => {
    postServiceMock.create.mockReturnValue(of({
      id: 1, title: 'titleOne', content: 'contentOne', date: '2026-01-15T10:00:00Z', author: 'authorOne', topicId: 10
    }));
    topicServiceMock.all.mockReturnValue(of(topicList));

    await TestBed.configureTestingModule({
      imports: [CreatepostComponent],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: postServiceMock },
        { provide: TopicService, useValue: topicServiceMock }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(CreatepostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create with an invalid empty form', () => {
    expect(component).toBeTruthy();
    expect(component['form'].invalid).toBe(true);
  });

  it('should load the available topics for the select', () => {
    expect(topicServiceMock.all).toHaveBeenCalled();
    expect(component['topics']()).toEqual(topicList);
  });

  it('should call create with the form value on submit', () => {
    component['form'].setValue({ title: 'titleOne', content: 'contentOne', topicId: 10 });

    component['onSubmit']();

    expect(postServiceMock.create).toHaveBeenCalledWith({ title: 'titleOne', content: 'contentOne', topicId: 10 });
  });

  it('should set onError when the creation fails', () => {
    postServiceMock.create.mockReturnValue(throwError(() => new Error('Bad Request')));
    component['form'].setValue({ title: 'titleOne', content: 'contentOne', topicId: 10 });

    component['onSubmit']();

    expect(component['onError']).toBe(true);
  });
});
