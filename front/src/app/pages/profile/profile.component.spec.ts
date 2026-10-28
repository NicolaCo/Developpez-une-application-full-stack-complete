import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of } from 'rxjs';
import { ProfileComponent } from './profile.component';
import { TopicService } from '../../core/service/topic.service';
import { UserService } from '../../core/service/user.service';
import { AuthService } from '../../core/service/auth.service';
import { Topic } from '../../core/model/topic.interface';
import { UserProfile } from '../../core/model/user.interface';

describe('ProfileComponent', () => {
  let component: ProfileComponent;
  let fixture: ComponentFixture<ProfileComponent>;
  const userServiceMock = { profile: vi.fn(), update: vi.fn() };
  const topicServiceMock = { all: vi.fn() };
  const authServiceMock = { updateToken: vi.fn() };

  const user: UserProfile = {
    id: 1,
    email: 'e2e@mdd.test',
    username: 'e2e_user'
  };

  const topicList: Topic[] = [
    { id: 10, title: 'Angular', description: 'Front framework', isSubscribed: true },
    { id: 20, title: 'Tests', description: 'Testing', isSubscribed: false }
  ];

  beforeEach(async () => {
    userServiceMock.profile.mockReturnValue(of(user));
    topicServiceMock.all.mockReturnValue(of(topicList));

    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
      providers: [
        { provide: UserService, useValue: userServiceMock },
        { provide: TopicService, useValue: topicServiceMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the current profile', () => {
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain(user.email);
    expect(text).toContain(user.username);
  });

  it('should display only the subscribed topics', () => {
    const text = fixture.nativeElement.textContent as string;

    expect(text).toContain('Angular');
    expect(text).not.toContain('Tests');
  });
});
