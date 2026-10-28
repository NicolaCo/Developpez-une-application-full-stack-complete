import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../model/auth.interface';

const TOKEN_KEY = 'mdd_token';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private httpClient = inject(HttpClient);

    private readonly tokenSignal = signal<string | null>(localStorage.getItem(TOKEN_KEY));

    public readonly token = this.tokenSignal.asReadonly();
    public readonly isLoggedIn = computed(() => this.tokenSignal() !== null);

    public login(request: LoginRequest): Observable<AuthResponse> {
        return this.httpClient.post<AuthResponse>(environment.apiUrl + 'auth/login', request)
            .pipe(tap(response => this.setToken(response.token)));
    }

    public register(request: RegisterRequest): Observable<AuthResponse> {
        return this.httpClient.post<AuthResponse>(environment.apiUrl + 'auth/register', request)
            .pipe(tap(response => this.setToken(response.token)));
    }

    public logout(): void {
        localStorage.removeItem(TOKEN_KEY);
        this.tokenSignal.set(null);
    }

    public updateToken(token: string): void {
        this.setToken(token);
    }

    private setToken(token: string): void {
        localStorage.setItem(TOKEN_KEY, token);
        this.tokenSignal.set(token);
    }
}
