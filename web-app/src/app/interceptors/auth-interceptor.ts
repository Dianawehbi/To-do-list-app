import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError, Observable, shareReplay } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

const AUTH_ENDPOINTS = [
  '/api/auth/login',
  '/api/auth/refresh',
  '/api/auth/register',
  '/api/auth/logout',
];

let refreshInProgress$: Observable<unknown> | null = null;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const isAuthEndpoint = AUTH_ENDPOINTS.some((url) => req.url.includes(url));

  const token = authService.accessToken();
  const authReq =
    token && !isAuthEndpoint
      ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (isAuthEndpoint) {
        return throwError(() => error);
      }

      if (error.status === 401) {
        if (!refreshInProgress$) {
          refreshInProgress$ = authService.refreshAccessToken().pipe(shareReplay(1));
        }
        console.log('hi  refresh in progress: true ');
        return refreshInProgress$.pipe(
          switchMap(() => {
            refreshInProgress$ = null;
            const newToken = authService.accessToken();
            const retriedReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` },
            });
            return next(retriedReq);
          }),
          catchError((refreshError) => {
            refreshInProgress$ = null;
            authService.logout();
            return throwError(() => refreshError);
          }),
        );
      }

      return throwError(() => error);
    }),
  );
};
