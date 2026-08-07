import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, firstValueFrom, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { Task, TaskRequestData, TaskStatus } from '../models/task.model';

import { ApiErrorResponse, PageResponse } from '../models/auth.model';


@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private http = inject(HttpClient);

  private readonly baseUrl = `${environment.taskApiUrl}/tasks`;

  tasks = signal<Task[]>([]);

  loading = signal(false);

  totalCount = signal(0);

  totalPages = signal(0);

  error = signal<string | null>(null);

  // GET /api/tasks?page=0&size=20&status=&priority=&categoryId=&search=
  getTasks(
    page = 0,
    size = 12,
    filters?: {
      status?: TaskStatus | 'ALL';
      priority?: string | 'ALL';
      categoryId?: number | 'ALL';
      search?: string;
    },
  ) {
    let params = new HttpParams().set('page', page).set('size', size);

    if (filters?.status && filters.status !== 'ALL') {
      params = params.set('status', filters.status);
    }

    if (filters?.priority && filters.priority !== 'ALL') {
      params = params.set('priority', filters.priority);
    }

    if (filters?.categoryId && filters.categoryId !== 'ALL') {
      params = params.set('categoryId', filters.categoryId);
    }

    if (filters?.search) {
      params = params.set('search', filters.search);
    }

    this.loading.set(true);

    return this.http.get<PageResponse<Task>>(this.baseUrl, { params }).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError = error.error as ApiErrorResponse;
        this.error.set(apiError.message ?? 'Failed to load tasks');
        return throwError(() => apiError);
      }),
    );
  }

  // GET /api/tasks/{id}
  async getTaskById(id: number): Promise<Task> {
    return await firstValueFrom(
      this.http.get<Task>(`${this.baseUrl}/${id}`).pipe(
        catchError((error: HttpErrorResponse) => {
          const apiError = error.error as ApiErrorResponse;
          return throwError(() => apiError);
        }),
      ),
    );
  }

  // POST /api/tasks
  async createTask(taskData: TaskRequestData): Promise<Task> {
    return await firstValueFrom(
      this.http.post<Task>(this.baseUrl, taskData).pipe(
        catchError((error: HttpErrorResponse) => {
          const apiError = error.error as ApiErrorResponse;
          return throwError(() => apiError);
        }),
      ),
    );
  }

  // PUT /api/tasks/{id}
  async updateTask(taskId: number, taskData: TaskRequestData): Promise<Task> {
    return await firstValueFrom(
      this.http.put<Task>(`${this.baseUrl}/${taskId}`, taskData).pipe(
        catchError((error: HttpErrorResponse) => {
          const apiError = error.error as ApiErrorResponse;
          return throwError(() => apiError);
        }),
      ),
    );
  }

  // PATCH /api/tasks/{id}/status  { "status": "DONE" }
  async updateStatus(taskId: number, status: TaskStatus): Promise<Task> {
    return await firstValueFrom(
      this.http.patch<Task>(`${this.baseUrl}/${taskId}/status`, { status }).pipe(
        catchError((error: HttpErrorResponse) => {
          const apiError = error.error as ApiErrorResponse;
          return throwError(() => apiError);
        }),
      ),
    );
  }

  // DELETE /api/tasks/{id}
  async deleteTask(taskId: number): Promise<void> {
    await firstValueFrom(
      this.http.delete<void>(`${this.baseUrl}/${taskId}`).pipe(
        catchError((error: HttpErrorResponse) => {
          const apiError = error.error as ApiErrorResponse;
          return throwError(() => apiError);
        }),
      ),
    );
  }
}
