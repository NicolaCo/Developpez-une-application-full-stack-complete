import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { AuthService } from '../service/auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;
  let router: Router;

  const loginPath = environment.apiUrl + 'auth/login';
  const topicsPath = environment.apiUrl + 'topics';
  const credentials = { email: 'user@example.com', password: 'password123' };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function login(): void {
    authService.login(credentials).subscribe();
    httpMock.expectOne(loginPath).flush({ token: 'a.jwt.token' });
  }

  function flush401(url: string): void {
    httpMock.expectOne(url)
      .flush({ status: 401, message: 'Unauthorized' }, { status: 401, statusText: 'Unauthorized' });
  }

  it('should not add an Authorization header when the user is logged out', () => {
    httpClient.get(topicsPath).subscribe();

    const req = httpMock.expectOne(topicsPath);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it('should add the Bearer token when the user is logged in', () => {
    login();

    httpClient.get(topicsPath).subscribe();

    const req = httpMock.expectOne(topicsPath);
    expect(req.request.headers.get('Authorization')).toBe('Bearer a.jwt.token');
    req.flush([]);
  });

  it('should never add the Authorization header on auth calls', () => {
    login();

    authService.login(credentials).subscribe();

    const req = httpMock.expectOne(loginPath);
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ token: 'a.jwt.token' });
  });

  it('should log out and redirect to login on a 401 response', () => {
    login();
    const navigateSpy = vi.spyOn(router, 'navigate');

    httpClient.get(topicsPath).subscribe({ error: () => undefined });
    flush401(topicsPath);

    expect(authService.isLoggedIn()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });

  it('should redirect only once for two simultaneous 401 responses', () => {
    login();
    const navigateSpy = vi.spyOn(router, 'navigate');

    httpClient.get(topicsPath).subscribe({ error: () => undefined });
    httpClient.get(topicsPath + '/1').subscribe({ error: () => undefined });
    flush401(topicsPath);
    flush401(topicsPath + '/1');

    expect(navigateSpy).toHaveBeenCalledTimes(1);
  });

  it('should not redirect on an error when the user was already logged out', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');

    httpClient.get(topicsPath)
      .subscribe({ error: (error: HttpErrorResponse) => expect(error.status).toBe(401) });
    flush401(topicsPath);

    expect(navigateSpy).not.toHaveBeenCalled();
  });
});
