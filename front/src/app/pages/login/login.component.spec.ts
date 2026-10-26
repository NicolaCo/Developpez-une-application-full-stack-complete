import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../core/service/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let router: Router;
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
    router = TestBed.inject(Router);
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

  it('should redirect to the home page after a direct login', () => {
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    component.form.setValue({ email: 'user@example.com', password: 'password123' });

    component.onSubmit();

    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });

  it('should redirect to the returnUrl after a login following a guard redirect', () => {
    vi.spyOn(router, 'parseUrl').mockReturnValue(router.parseUrl('/login?returnUrl=%2Ftopics'));
    const navigateByUrlSpy = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component.form.setValue({ email: 'user@example.com', password: 'password123' });

    component.onSubmit();

    expect(navigateByUrlSpy).toHaveBeenCalledWith('/topics');
  });

  it('should redirect to the home page for an external returnUrl', () => {
    vi.spyOn(router, 'parseUrl').mockReturnValue(router.parseUrl('/login?returnUrl=https%3A%2F%2Fevil.example'));
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    component.form.setValue({ email: 'user@example.com', password: 'password123' });

    component.onSubmit();

    expect(navigateSpy).toHaveBeenCalledWith(['/']);
  });
});
