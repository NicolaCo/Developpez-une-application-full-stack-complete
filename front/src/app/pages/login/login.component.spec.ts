import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/service/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  const authServiceMock = { login: vi.fn(), token: vi.fn(), isLoggedIn: vi.fn() };

  beforeEach(async () => {
    authServiceMock.login.mockReturnValue(of({ token: 'a.jwt.token' }));
    authServiceMock.token.mockReturnValue(null);
    authServiceMock.isLoggedIn.mockReturnValue(false);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: authServiceMock }
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create with an invalid empty form', () => {
    expect(component).toBeTruthy();
    expect(component.form.invalid).toBe(true);
  });

  it('should call login with the form value on submit', () => {
    component.form.setValue({ email: 'user@example.com', password: 'password123' });

    component.onSubmit();

    expect(authServiceMock.login).toHaveBeenCalledWith({ email: 'user@example.com', password: 'password123' });
  });

  it('should set onError when the login fails', () => {
    authServiceMock.login.mockReturnValue(throwError(() => new Error('Unauthorized')));
    component.form.setValue({ email: 'user@example.com', password: 'password123' });

    component.onSubmit();

    expect(component.onError).toBe(true);
  });
});
