import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TopiccardComponent } from './topiccard.component';

describe('TopiccardComponent', () => {
  let component: TopiccardComponent;
  let fixture: ComponentFixture<TopiccardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopiccardComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(TopiccardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
