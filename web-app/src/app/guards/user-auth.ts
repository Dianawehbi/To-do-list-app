import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const userGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isUser = !authService.isAdmin();
  if (isUser) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
