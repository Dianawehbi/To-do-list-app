import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isAdmin = authService.isAdmin();
  console.log('Is admin  ? ' + isAdmin);
  if (isAdmin) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
