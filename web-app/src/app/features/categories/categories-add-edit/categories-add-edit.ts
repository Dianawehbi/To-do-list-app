import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ApiErrorResponse } from '../../../models/auth.model';
import { CategoryService } from '../../../services/CategoryService';

@Component({
  selector: 'app-categories-new',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './categories-add-edit.html',
  styleUrl: './categories-add-edit.css',
})
export class CategoriesAddEditComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private categoryService = inject(CategoryService);
  private destroyRef = inject(DestroyRef);

  categoryId: number | null = null;

  isLoading = signal(false);
  isSubmitting = signal(false);
  submitError = signal<string | null>(null);

  isEditMode = signal(false);
  categoryNotFound = signal(false);

  form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.maxLength(50)],
    }),

    color: new FormControl('#4a90d9', {
      nonNullable: true,
      validators: [Validators.required],
    }),

    active: new FormControl(true, {
      nonNullable: true,
    }),
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.categoryId = Number(idParam);
      this.isEditMode.set(true);
      this.loadCategory();
    }
  }

  private loadCategory(): void {
    this.isLoading.set(true);

    const subscription = this.categoryService.getCategoryById(this.categoryId!).subscribe({
      next: (category) => {
        this.form.patchValue({
          name: category.name,
          color: category.color,
          active: category.active,
        });
        this.isLoading.set(false);
      },
      error: (error: ApiErrorResponse) => {
        this.submitError.set(error.message ?? 'Unable to load category');
        this.categoryNotFound.set(true);
        this.isLoading.set(false);
      },
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();

    this.isSubmitting.set(true);
    this.submitError.set(null);

    const request$ = this.isEditMode()
      ? this.categoryService.updateCategory(this.categoryId!, value)
      : this.categoryService.createCategory(value);

    const subscription = request$.subscribe({
      next: () => {
        this.router.navigate(['/categories']);
      },
      error: (error: ApiErrorResponse) => {
        this.isSubmitting.set(false);
        this.submitError.set(
          error.message ??
            (this.isEditMode()
              ? 'Could not update the category. Please try again.'
              : 'Could not create the category. Please try again.'),
        );
      },
    });

    this.destroyRef.onDestroy(() => subscription.unsubscribe());
  }

  get nameIsInvalid(): boolean {
    const control = this.form.controls.name;
    return control.touched && control.dirty && control.invalid;
  }

  get colorIsInvalid(): boolean {
    const control = this.form.controls.color;
    return control.touched && control.dirty && control.invalid;
  }
}
