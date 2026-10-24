import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../model/auth.interface';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let authService: AuthService;
  let httpMock: HttpTestingController;

  const loginRequest: LoginRequest = { email: 'user@example.com', password: 'password123' };
  const registerRequest: RegisterRequest = { email: 'user@example.com', username: 'userone', password: 'password123' };
  const authResponse: AuthResponse = { token: 'a.jwt.token' };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService
      ]
    });
    authService = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created and logged out by default', () => {
    expect(authService).toBeTruthy();
    expect(authService.token()).toBeNull();
    expect(authService.isLoggedIn()).toBe(false);
  });

  it('login should post credentials and store the token', () => {
    authService.login(loginRequest).subscribe();

    const req = httpMock.expectOne(environment.apiUrl + 'auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(loginRequest);
    req.flush(authResponse);

    expect(authService.token()).toBe(authResponse.token);
    expect(authService.isLoggedIn()).toBe(true);
    expect(localStorage.getItem('mdd_token')).toBe(authResponse.token);
  });

  it('register should post the new user and store the token', () => {
    authService.register(registerRequest).subscribe();

    const req = httpMock.expectOne(environment.apiUrl + 'auth/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(registerRequest);
    req.flush(authResponse);

    expect(authService.token()).toBe(authResponse.token);
    expect(authService.isLoggedIn()).toBe(true);
    expect(localStorage.getItem('mdd_token')).toBe(authResponse.token);
  });

  it('logout should clear the token and the storage', () => {
    authService.login(loginRequest).subscribe();
    httpMock.expectOne(environment.apiUrl + 'auth/login').flush(authResponse);

    authService.logout();

    expect(authService.token()).toBeNull();
    expect(authService.isLoggedIn()).toBe(false);
    expect(localStorage.getItem('mdd_token')).toBeNull();
  });

  describe('when a token is already stored', () => {
    beforeEach(() => {
      localStorage.setItem('mdd_token', 'stored.token');
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        providers: [
          provideHttpClient(),
          provideHttpClientTesting(),
          AuthService
        ]
      });
      authService = TestBed.inject(AuthService);
      httpMock = TestBed.inject(HttpTestingController);
    });

    it('should restore the token from localStorage on creation', () => {
      expect(authService.token()).toBe('stored.token');
      expect(authService.isLoggedIn()).toBe(true);
    });
  });
});
