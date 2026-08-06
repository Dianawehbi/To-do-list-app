import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { ApiErrorResponse, PageResponse } from '../models/auth.model';
import { User, UserRequestData } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private http = inject(HttpClient);

  private readonly baseUrl = `${environment.authApiUrl}/users`;

  // GET /api/users -  Admin only
  getAllUsers(page = 0, size = 10) {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http.get<PageResponse<User>>(this.baseUrl, { params }).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;

        return throwError(() => apiError);
      }),
    );
  }

  // GET /api/users/{id}
  getUserById(id: number) {
    return this.http.get<User>(`${this.baseUrl}/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;

        return throwError(() => apiError);
      }),
    );
  }

  // PUT /api/users/{id}
  updateUser(id: number, data: UserRequestData) {
    return this.http.put<User>(`${this.baseUrl}/${id}`, data).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;

        return throwError(() => apiError);
      }),
    );
  }

  // DELETE /api/users/{id}
  // Backend only disables the user
  deleteUser(id: number) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;

        return throwError(() => apiError);
      }),
    );
  }
}
