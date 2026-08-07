import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { TaskService } from '../../../services/task.service';
import { CategoryService } from '../../../services/CategoryService';
import { Category } from '../../../models/category.model';
import { TaskPriority, TaskStatus } from '../../../models/task.model';
import { ApiErrorResponse } from '../../../models/auth.model';

@Component({
  selector: 'app-task-new',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './task-new.html',
  styleUrl: './task-new.css',
})
export class TaskNewComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private categoryService = inject(CategoryService);
  private taskService = inject(TaskService);
  private router = inject(Router);

  categories = signal<Category[]>([]);
  isLoading = signal(false);
  isSubmitting = signal(false);
  submitError = signal<string | null>(null);

  taskId: number | null = null;
  isEditMode = signal(false);
  taskNotFound = signal(false);

  form = new FormGroup({
    title: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(150)],
    }),
    description: new FormControl('', {
      validators: [Validators.maxLength(1000)],
    }),
    status: new FormControl<TaskStatus>('TODO', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    priority: new FormControl<TaskPriority>('MEDIUM', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    dueDate: new FormControl('', {
      nonNullable: true,
    }),
    categoryId: new FormControl<string>('', {
      nonNullable: true,
    }),
  });

  ngOnInit(): void {
    this.categoryService.getAllCategories(0, 100, true).subscribe({
      next: (response) => this.categories.set(response.content),
      error: () => this.submitError.set('Could not load categories.'),
    });

    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.taskId = Number(idParam);
      this.isEditMode.set(true);
      this.loadTask();
    }
  }

  private async loadTask(): Promise<void> {
    this.isLoading.set(true);

    try {
      const task = await this.taskService.getTaskById(this.taskId!);

      this.form.patchValue({
        title: task.title,
        description: task.description ?? '',
        status: task.status,
        priority: task.priority,
        dueDate: task.dueDate?.toString() ?? undefined,
        categoryId: task.categoryId ? String(task.categoryId) : '',
      });
    } catch (error) {
      this.submitError.set((error as ApiErrorResponse).message ?? 'Unable to load task');
      this.taskNotFound.set(true);
    } finally {
      this.isLoading.set(false);
    }
  }

  async onSubmit(): Promise<void> {
    if (this.form.invalid) {
      this.submitError.set('Please fill in the required fields before submitting.');
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();

    this.isSubmitting.set(true);
    this.submitError.set(null);

    const payload = {
      title: value.title,
      description: value.description || null,
      status: value.status,
      priority: value.priority,
      dueDate: value.dueDate || null,
      categoryId: value.categoryId ? Number(value.categoryId) : null,
    };

    try {
      if (this.isEditMode()) {
        await this.taskService.updateTask(this.taskId!, payload);
      } else {
        await this.taskService.createTask(payload);
      }

      this.router.navigate(['/tasks']);
    } catch (error) {
      this.handleError(error as ApiErrorResponse);
    } finally {
      this.isSubmitting.set(false);
    }
  }

  private handleError(err: ApiErrorResponse) {
    if (err.fieldErrors) {
      for (const [field, message] of Object.entries(err.fieldErrors)) {
        if (field in this.form.controls) {
          (this.form.controls as any)[field].setErrors({ server: message });
          this.submitError.set(message);
        }
      }
      return;
    }

    this.submitError.set(
      err.message ??
        (this.isEditMode()
          ? 'Could not update the task. Please try again.'
          : 'Could not create the task. Please try again.'),
    );
  }

  onCancel(): void {
    this.router.navigate(['/tasks']);
  }

  get titleIsInvalid(): boolean {
    const control = this.form.controls.title;
    return control.touched && control.dirty && control.invalid;
  }
}