import { afterNextRender, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { debounceTime, of } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ApiErrorResponse, LoginRequest } from '../../models/auth.model';

let initialUSerNameValue = '';
const savedForm = window.localStorage.getItem('saved-login-form');
if (savedForm) {
  initialUSerNameValue = JSON.parse(savedForm).username;
}

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  private destroyRef = inject(DestroyRef);
  private router = inject(Router);
  authService = inject(AuthService);

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  form = new FormGroup({
    username: new FormControl(initialUSerNameValue, {
      validators: [Validators.required, Validators.minLength(3)],
    }),
    password: new FormControl('', {
      validators: [Validators.required, Validators.minLength(6)],
    }),
  });

  constructor() {
    afterNextRender(() => {
      const savedForm = window.localStorage.getItem('saved-login-form');
      if (savedForm) {
        const saved = JSON.parse(savedForm);
        setTimeout(() => {
          this.form.controls.username.setValue(saved.username);
        }, 1);
      }

      const subscription = this.form.valueChanges?.pipe(debounceTime(500)).subscribe({
        next: (value) =>
          window.localStorage.setItem(
            'saved-login-form',
            JSON.stringify({ username: value.username }),
          ),
      });

      this.destroyRef.onDestroy(() => subscription?.unsubscribe());
    });
  }

  onSubmit() {
    if (this.usernameIsInvalid || this.passwordIsInvalid) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const data: LoginRequest = {
      username: this.form.controls.username.value!,
      password: this.form.controls.password.value!,
    };

    this.authService.login(data).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigate(['/tasks']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.handleLoginError(err);
      },
    });
  }

  private handleLoginError(err: ApiErrorResponse) {
    if (err.fieldErrors) {
      for (const [field, message] of Object.entries(err.fieldErrors)) {
        if (field in this.form.controls) {
          (this.form.controls as any)[field].setErrors({ server: message });
        }
      }
      return;
    }

    this.errorMessage.set(err.message ?? 'Invalid credentials.');
  }

  toRegister() {
    this.router.navigate(['/register']);
  }

  get usernameIsInvalid() {
    return (
      this.form.controls.username.touched &&
      this.form.controls.username.dirty &&
      this.form.controls.username.invalid
    );
  }

  get passwordIsInvalid() {
    return (
      this.form.controls.password.touched &&
      this.form.controls.password.dirty &&
      this.form.controls.password.invalid
    );
  }
}
