import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, firstValueFrom, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { Category } from '../models/category.model';
import { ApiErrorResponse } from '../models/auth.model';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface CategoryRequest {
  name: string;
  color: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class CategoryService {
  private http = inject(HttpClient);

  private readonly baseUrl = `${environment.taskApiUrl}/categories`;

  getAllCategories(page = 0, size = 12, active?: boolean) {
    let params = new HttpParams().set('page', page).set('size', size);

    if (active !== undefined) {
      params = params.set('active', active);
    }

    return this.http.get<PageResponse<Category>>(this.baseUrl, { params }).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;
        return throwError(() => apiError);
      }),
    );
  }

  getCategoryById(id: number) {
    return this.http.get<Category>(`${this.baseUrl}/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;
        return throwError(() => apiError);
      }),
    );
  }

  createCategory(data: CategoryRequest) {
    return this.http.post<Category>(this.baseUrl, data).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;
        return throwError(() => apiError);
      }),
    );
  }

  updateCategory(id: number, data: CategoryRequest) {
    return this.http.put<Category>(`${this.baseUrl}/${id}`, data).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;
        return throwError(() => apiError);
      }),
    );
  }

  async deleteCategory(id: number): Promise<void> {
    await firstValueFrom(
      this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(
        catchError((error: HttpErrorResponse) => {
          const apiError = error.error as ApiErrorResponse;
          return throwError(() => apiError);
        }),
      ),
    );
  }
}
