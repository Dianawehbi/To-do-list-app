import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, firstValueFrom, switchMap, tap, throwError } from 'rxjs';
import {
  ApiErrorResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
} from '../models/auth.model';
import { User } from '../models/user.model';
import { environment } from '../../environments/environment';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';
const EXPIRES_AT_KEY = 'expiresAt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private readonly baseUrl = environment.authApiUrl;

  private tokenExpirationTimer: ReturnType<typeof setTimeout> | undefined;

  currentUser = signal<User | null>(null);
  accessToken = signal<string | null>(null);
  isLoggedIn = computed(() => this.currentUser() !== null);
  isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  errorMessage = signal<string | null>(null);

  login(loginRequest: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/login`, loginRequest).pipe(
      tap((response) => this.handleAuthentication(response)),
      switchMap(() => this.fetchCurrentUser()),
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;
        return throwError(() => apiError);
      }),
    );
  }

  register(regRequest: RegisterRequest) {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/register`, regRequest).pipe(
      tap((response) => this.handleAuthentication(response)),
      switchMap(() => this.fetchCurrentUser()),
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;
        return throwError(() => apiError);
      }),
    );
  }

  refreshAccessToken() {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    console.log(' refresh access token service ');

    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/refresh`, { refreshToken }).pipe(
      tap((response) => this.handleAuthentication(response)),
      switchMap(() => this.fetchCurrentUser()),
    );
  }

  logout() {
    console.log('LOGOUT');
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

    this.http.post(`${this.baseUrl}/auth/logout`, { refreshToken }).subscribe({
      error: () => {},
    });

    this.clearSession();
    this.router.navigate(['/login']);
  }

  // Called once on ApplicationConfig
  async autoLogin(): Promise<void> {
    console.log('Auto Log in ');
    const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
    const expiresAt = localStorage.getItem(EXPIRES_AT_KEY);

    if (!accessToken || !expiresAt) return;

    const expirationDuration = +expiresAt - new Date().getTime();

    if (expirationDuration <= 0) return;

    this.accessToken.set(accessToken);

    try {
      await firstValueFrom(this.fetchCurrentUser());
    } catch {
      return;
    }
    this.autoLogout(expirationDuration);
    console.log('Auto Log in - successfuly');
  }

  private handleAuthentication(response: LoginResponse): void {
    console.log(response);
    const expiresAt = response.accessTokenExpiresAt * 1000;
    const expirationDuration = expiresAt - new Date().getTime();

    this.accessToken.set(response.accessToken);

    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);
    localStorage.setItem(EXPIRES_AT_KEY, expiresAt.toString());

    this.autoLogout(expirationDuration);

    // this.fetchCurrentUser().subscribe();
  }

  fetchCurrentUser() {
    return this.http.get<User>(`${this.baseUrl}/auth/me`).pipe(
      tap((user) => {
        this.currentUser.set(user);
      }),
      catchError((error: HttpErrorResponse) => {
        return throwError(() => error);
      }),
    );
  }

  // private autoLogout(expirationDuration: number): void {
  //   console.log("auto logout");
  //   if (this.tokenExpirationTimer) {
  //     clearTimeout(this.tokenExpirationTimer);
  //   }

  //   //  Prevent instant logout
  //   if (expirationDuration <= 0) {
  //     this.logout();
  //     return;
  //   }

  //   this.tokenExpirationTimer = setTimeout(() => {
  //     this.logout();
  //   }, expirationDuration);
  // }

  private autoLogout(expirationDuration: number): void {
    if (this.tokenExpirationTimer) {
      clearTimeout(this.tokenExpirationTimer);
    }
    // No forced logout here —  the interceptor's reactive 401-> refresh
  }

  private clearSession(): void {
    this.currentUser.set(null);
    this.accessToken.set(null);
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(EXPIRES_AT_KEY);
    if (this.tokenExpirationTimer) {
      clearTimeout(this.tokenExpirationTimer);
    }
    this.tokenExpirationTimer = undefined;
  }
}
