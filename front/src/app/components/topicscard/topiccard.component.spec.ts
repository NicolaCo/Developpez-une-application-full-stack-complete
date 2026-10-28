import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TopiccardComponent } from './topiccard.component';
import { Topic } from '../../core/model/topic.interface';

describe('TopiccardComponent', () => {
  let component: TopiccardComponent;
  let fixture: ComponentFixture<TopiccardComponent>;

  const topic: Topic = {
    id: 1,
    title: 'Angular',
    description: 'Front framework',
    isSubscribed: true
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopiccardComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TopiccardComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('topic', topic);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
