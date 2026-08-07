import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

const AUTH_ENDPOINTS = ['/api/auth/login', '/api/auth/refresh', '/api/auth/register'];

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
      // Don't attempt a refresh loop on the refresh/login calls themselves
      console.log('Interceptor file error : ');
      console.log(error);
      if (isAuthEndpoint) {
        return throwError(() => error);
      }

      if (error.status === 401) {
        return authService.refreshAccessToken().pipe(
          switchMap(() => {
            const newToken = authService.accessToken();
            const retriedReq = req.clone({
              setHeaders: { Authorization: `Bearer ${newToken}` },
            });
            return next(retriedReq);
          }),
          catchError((refreshError) => {
            authService.logout();
            return throwError(() => refreshError);
          }),
        );
      }
      return throwError(() => error);
    }),
  );
};
