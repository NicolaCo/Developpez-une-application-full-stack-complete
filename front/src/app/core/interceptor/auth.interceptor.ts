import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../service/auth.service';

const AUTH_PATH = '/auth/';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const token = authService.token();

    const authenticatedRequest = token && !request.url.includes(AUTH_PATH)
        ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
        : request;

    return next(authenticatedRequest).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 && authService.isLoggedIn()) {
                authService.logout();
                router.navigate(['/login']);
            }
            return throwError(() => error);
        })
    );
};
