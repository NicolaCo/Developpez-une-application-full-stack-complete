import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { AuthService } from '../service/auth.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {

  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/topics' } as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter([])]
    });
  });

  it('should redirect to login with the returnUrl when the user is logged out', () => {
    const result = TestBed.runInInjectionContext(() => authGuard(route, state));

    expect(TestBed.inject(AuthService).isLoggedIn()).toBe(false);
    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/login?returnUrl=%2Ftopics');
  });

  describe('when the user is logged in', () => {
    beforeEach(() => {
      localStorage.setItem('mdd_token', 'a.jwt.token');
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({ providers: [provideRouter([])] });
    });

    it('should allow access', () => {
      const result = TestBed.runInInjectionContext(() => authGuard(route, state));

      expect(TestBed.inject(AuthService).isLoggedIn()).toBe(true);
      expect(result).toBe(true);
    });
  });
});
