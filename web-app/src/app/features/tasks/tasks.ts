import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

import { TaskCardComponent } from './task-card/task-card';

import { Task, TaskStatus } from '../../models/task.model';
import { Category } from '../../models/category.model';
import { ApiErrorResponse } from '../../models/auth.model';

import { TaskService } from '../../services/task.service';
import { CategoryService } from '../../services/CategoryService';

import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-tasks',
  imports: [TaskCardComponent, ReactiveFormsModule],
  templateUrl: './tasks.html',
  styleUrl: './tasks.css',
})
export class TasksComponent implements OnInit {
  private taskService = inject(TaskService);
  private categoryService = inject(CategoryService);
  private authService = inject(AuthService);
  private router = inject(Router);

  tasks = signal<Task[]>([]);
  categories = signal<Category[]>([]);

  isAdmin = computed(() => this.authService.isAdmin());

  currentPage = signal(0);
  pageSize = signal(12);
  totalPages = signal(0);

  selectedStatus: TaskStatus | 'ALL' = 'ALL';
  selectedCategoryId: number | 'ALL' = 'ALL';
  searchTerm = signal('');

  searchControl = new FormControl('');

  categoryActiveFilter = new FormControl('all');

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  isMutating = signal(false);

  constructor() {
    this.categoryActiveFilter.valueChanges.subscribe((value) => {
      if (value != null) {
        this.filterCategories(value);
      }
    });

    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe((value) => {
        this.searchTerm.set(value ?? '');
        this.currentPage.set(0);
        this.loadTasks();
      });
  }

  ngOnInit() {
    this.loadTasks();
    this.loadCategories();
  }

  loadTasks() {
    this.isLoading.set(true);
    this.errorMessage.set(null);
    console.log('load tasks ' + this.searchTerm());
    this.taskService
      .getTasks(this.currentPage(), this.pageSize(), {
        status: this.selectedStatus,
        categoryId: this.selectedCategoryId,
        search: this.searchTerm(),
      })
      .subscribe({
        next: (response) => {
          this.tasks.set(response.content);
          this.totalPages.set(response.totalPages);
          this.isLoading.set(false);
        },
        error: (error: ApiErrorResponse) => {
          this.errorMessage.set(error.message ?? 'Failed to load tasks');
          this.isLoading.set(false);
        },
      });
  }

  loadCategories(active?: boolean) {
    this.categoryService.getAllCategories(0, 100, active).subscribe({
      next: (response) => {
        this.categories.set(response.content);
      },
    });
  }

  filterCategories(value: string) {
    if (value === 'active') {
      this.loadCategories(true);
    } else if (value === 'inactive') {
      this.loadCategories(false);
    } else {
      this.loadCategories(undefined);
    }
  }

  onStatusChange(value: string) {
    this.selectedStatus = value as TaskStatus | 'ALL';
    this.currentPage.set(0);
    this.loadTasks();
  }

  onCategoryChange(value: string) {
    this.selectedCategoryId = value === 'ALL' ? 'ALL' : Number(value);
    this.currentPage.set(0);
    this.loadTasks();
  }


  onAddTask() {
    this.router.navigate(['/tasks/new']);
  }

  // PATCH /api/tasks/{id}/status
  async onStatusPatch(taskId: number, status: TaskStatus) {
    this.errorMessage.set(null);

    try {
      const updated = await this.taskService.updateStatus(taskId, status);

      // update the task in place instead of refetching the whole page
      this.tasks.update((tasks) => tasks.map((t) => (t.id === taskId ? updated : t)));
    } catch (error) {
      this.errorMessage.set((error as ApiErrorResponse).message ?? 'Failed to update status');
    }
  }

  // DELETE /api/tasks/{id}
  async onDeleteTask(taskId: number) {
    this.isMutating.set(true);
    this.errorMessage.set(null);

    try {
      await this.taskService.deleteTask(taskId);
      await this.loadTasks();
    } catch (error) {
      this.errorMessage.set((error as ApiErrorResponse).message ?? 'Failed to delete task');
    } finally {
      this.isMutating.set(false);
    }
  }

  nextPage() {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update((p) => p + 1);
      this.loadTasks();
    }
  }

  previousPage() {
    if (this.currentPage() > 0) {
      this.currentPage.update((p) => p - 1);
      this.loadTasks();
    }
  }
}
