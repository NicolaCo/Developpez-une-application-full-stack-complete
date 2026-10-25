import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TopicsComponent } from './topics.component';
import { TopicService } from '../../core/service/topic.service';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import { Topic } from '../../core/model/topic.interface';

describe('TopicsComponent', () => {
  let component: TopicsComponent;
  let fixture: ComponentFixture<TopicsComponent>;
  const topicServiceMock = { all: vi.fn() };

  const topicList: Topic[] = [
    {id: 1, description: 'Description One', title: 'AwsomeOne', isSubscribed: true},
    {id: 2, description: 'Description Two', title: 'AwsomeTwo', isSubscribed: false}
  ];

  beforeEach(async () => {
    topicServiceMock.all.mockReturnValue(of(topicList));

    await TestBed.configureTestingModule({
      imports: [TopicsComponent],
      providers: [{ provide: TopicService, useValue: topicServiceMock }],
    })
      .compileComponents();

    fixture = TestBed.createComponent(TopicsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should display the topics', () => {
      const text = fixture.nativeElement.textContent as string;

      expect(text).toContain('AwsomeOne');
      expect(text).toContain('AwsomeTwo');
    });

    it('should show the subscribe button', () => {
      // TODO 
      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain('S\'abonner');
    });

    it('should show the already subscribed button when the user has already subscribe', () => {
      // TODO 
      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain('Déjà abonné');
    });
  });  
  
});
