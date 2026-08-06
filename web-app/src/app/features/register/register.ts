import {
  afterNextRender,
  Component,
  DestroyRef,
  DoCheck,
  inject,
  OnChanges,
  signal,
  SimpleChanges,
} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { ApiErrorResponse, RegisterRequest } from '../../models/auth.model';

function equalValues(controlName1: string, controlName2: string) {
  return (control: AbstractControl) => {
    const val1 = control.get(controlName1)?.value;
    const val2 = control.get(controlName2)?.value;
    return val1 === val2 ? null : { valuesNotEqual: true };
  };
}

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class RegisterComponent {
  private destroyRef = inject(DestroyRef);
  private router = inject(Router);
  private authService = inject(AuthService);

  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  form = new FormGroup({
    email: new FormControl('', {
      validators: [Validators.email, Validators.required],
    }),
    username: new FormControl('', {
      validators: [Validators.required, Validators.minLength(3)],
    }),
    passwords: new FormGroup(
      {
        password: new FormControl('', {
          validators: [Validators.required, Validators.minLength(6)],
        }),
        confirmPassword: new FormControl('', { validators: [Validators.required] }),
      },
      { validators: [equalValues('password', 'confirmPassword')] },
    ),
  });

  constructor() {
    afterNextRender(() => {
      const savedForm = window.localStorage.getItem('saved-register-form');
      if (savedForm) {
        const saved = JSON.parse(savedForm);
        setTimeout(() => {
          this.form.controls.email.setValue(saved.email);
          this.form.controls.username.setValue(saved.username);
        }, 1);
      }

      const subscription = this.form.valueChanges?.pipe(debounceTime(500)).subscribe({
        next: (value) =>
          window.localStorage.setItem(
            'saved-register-form',
            JSON.stringify({ username: value.username, email: value.email }),
          ),
      });

      this.destroyRef.onDestroy(() => subscription?.unsubscribe());
    });
  }

  onSubmit() {
    if (
      this.emailIsInvalid ||
      this.usernameIsInvalid ||
      this.passwordIsInvalid ||
      this.passwordsDoNotMatch
    ) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const data: RegisterRequest = {
      username: this.form.controls.username.value!,
      email: this.form.controls.email.value!,
      password: this.form.controls.passwords.controls.password.value!,
    };

    this.authService.register(data).subscribe({
      next: () => {
        console.log('inside register componenet ');
        this.isSubmitting.set(false);
        this.router.navigate(['/tasks']);
      },
      error: (err: ApiErrorResponse) => {
        this.isSubmitting.set(false);
        this.handleRegisterError(err);
      },
    });
  }

  private handleRegisterError(err: ApiErrorResponse) {
    // Field-level errors (400 validation) - attach to the actual controls
    if (err.fieldErrors) {
      for (const [field, message] of Object.entries(err.fieldErrors)) {
        if (field === 'password') {
          this.form.controls.passwords.controls.password.setErrors({ server: message });
        } else if (field in this.form.controls) {
          (this.form.controls as any)[field].setErrors({ server: message });
        }
      }
      return;
    }

    // Top-level error (409 duplicate emai)
    this.errorMessage.set(err.message ?? 'Something went wrong. Please try again.');
  }

  get emailIsInvalid() {
    return (
      this.form.controls.email.touched &&
      this.form.controls.email.dirty &&
      this.form.controls.email.invalid
    );
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
      this.form.controls.passwords.controls.password.touched &&
      this.form.controls.passwords.controls.password.dirty &&
      this.form.controls.passwords.controls.password.invalid
    );
  }

  get confirmPasswordIsInvalid() {
    return (
      this.form.controls.passwords.controls.confirmPassword.touched &&
      this.form.controls.passwords.controls.confirmPassword.dirty &&
      this.form.controls.passwords.controls.confirmPassword.invalid
    );
  }
  get passwordsDoNotMatch() {
    return (
      this.form.controls.passwords.controls.password.touched &&
      this.form.controls.passwords.controls.confirmPassword.touched &&
      this.form.controls.passwords.dirty &&
      this.form.controls.passwords.controls.confirmPassword.value != '' &&
      this.form.controls.passwords.errors?.['valuesNotEqual']
    );
  }

  toLogin() {
    this.router.navigate(['/login']);
  }
}
