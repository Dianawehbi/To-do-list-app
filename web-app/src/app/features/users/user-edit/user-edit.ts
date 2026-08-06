import { Component, inject, signal } from '@angular/core';

import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { UserRole } from '../../../models/user.model';
import { ApiErrorResponse } from '../../../models/auth.model';
import { UserService } from '../../../services/UserService';

@Component({
  selector: 'app-user-edit',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user-edit.html',
  styleUrl: './user-edit.css',
})
export class UserEditComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private userService = inject(UserService);

  userId!: number;

  isLoading = signal(false);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  userNotFound = signal(false);

  roles: UserRole[] = ['USER', 'ADMIN'];

  form = new FormGroup({
    username: new FormControl('', [Validators.required, Validators.minLength(3)]),
    email: new FormControl('', [Validators.required, Validators.email]),
    role: new FormControl<UserRole>('USER', Validators.required),
    enabled: new FormControl(true, Validators.required),
  });

  ngOnInit() {
    this.userId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadUser();
  }

  loadUser() {
    this.isLoading.set(true);

    this.userService.getUserById(this.userId).subscribe({
      next: (user) => {
        this.form.patchValue({
          username: user.username,
          email: user.email,
          role: user.role,
          enabled: user.enabled,
        });

        this.isLoading.set(false);
      },

      error: (error: ApiErrorResponse) => {
        this.errorMessage.set(error.message ?? 'Unable to load user');
        if (error.status === 404) {
          this.userNotFound.set(true);
        }
        this.isLoading.set(false);
      },
    });
  }

  async onSubmit() {
    if (this.form.invalid) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    try {
      console.log(this.userId);
      await this.userService.updateUser(this.userId, this.form.value as any);
      this.router.navigate(['/users']);
    } catch (error) {
      this.handleEditError(error as ApiErrorResponse);
    } finally {
      this.isSubmitting.set(false);
    }
  }

  private handleEditError(err: ApiErrorResponse) {
    if (err.fieldErrors) {
      for (const [field, message] of Object.entries(err.fieldErrors)) {
        if (field in this.form.controls) {
          (this.form.controls as any)[field].setErrors({ server: message });
        }
      }
      return;
    }

    this.errorMessage.set(err.message ?? 'Failed to update user');
  }

  cancel() {
    this.router.navigate(['/users']);
  }

  get usernameInvalid() {
    return this.form.controls.username.touched && this.form.controls.username.invalid;
  }

  get emailInvalid() {
    return this.form.controls.email.touched && this.form.controls.email.invalid;
  }
}
