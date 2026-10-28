import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/service/auth.service';
import { SignupComponent } from './signup.component';

describe('SignupComponent', () => {
  let component: SignupComponent;
  let fixture: ComponentFixture<SignupComponent>;
  const authServiceMock = { register: vi.fn(), token: vi.fn(), isLoggedIn: vi.fn() };
  const validPassword = 'Password123!';

  beforeEach(async () => {
    authServiceMock.register.mockReturnValue(of({ token: 'a.jwt.token' }));
    authServiceMock.token.mockReturnValue(null);
    authServiceMock.isLoggedIn.mockReturnValue(false);

    await TestBed.configureTestingModule({
      imports: [SignupComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(SignupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create with an invalid empty form', () => {
    expect(component).toBeTruthy();
    expect(component.form.invalid).toBe(true);
  });

  it('should call register with the form value on submit', () => {
    component.form.setValue({ email: 'user@example.com', username: 'userone', password: validPassword });

    component.onSubmit();

    expect(authServiceMock.register).toHaveBeenCalledWith({
      email: 'user@example.com',
      username: 'userone',
      password: validPassword
    });
  });

  it('should set onError when the registration fails', () => {
    authServiceMock.register.mockReturnValue(throwError(() => new Error('Conflict')));
    component.form.setValue({ email: 'user@example.com', username: 'userone', password: validPassword });

    component.onSubmit();

    expect(component.onError).toBe(true);
  });
});
