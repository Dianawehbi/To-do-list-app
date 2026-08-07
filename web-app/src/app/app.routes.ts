import { Routes } from '@angular/router';
import { authGuard } from './guards/auth-guard';
import { adminGuard } from './guards/admin-guard';
import { LoginComponent } from './features/login/login';
import { MainLayoutComponent } from './main-layout/main-layout';
import { RegisterComponent } from './features/register/register';
import { userGuard } from './guards/user-auth';

export const routes: Routes = [
  // No header — outside the layout
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  // Everything below gets the header, since it's nested under MainLayoutComponent
  {
    path: '',
    component: MainLayoutComponent,
    canActivateChild: [authGuard],
    children: [
      { path: '', redirectTo: 'tasks', pathMatch: 'full' },
      {
        path: 'tasks',
        loadComponent: () => import('./features/tasks/tasks').then((m) => m.TasksComponent),
      },
      {
        path: 'tasks/new',
        loadComponent: () =>
          import('./features/tasks/task-new/task-new').then((m) => m.TaskNewComponent),
        canActivate: [userGuard],
      },
      {
        path: 'tasks/:id/edit',
        loadComponent: () =>
          import('./features/tasks/task-new/task-new').then((m) => m.TaskNewComponent),
        canActivate: [userGuard],
      },
      {
        path: 'categories/:id/edit',
        loadComponent: () =>
          import('./features/categories/categories-add-edit/categories-add-edit').then(
            (m) => m.CategoriesAddEditComponent,
          ),
        canActivate: [adminGuard],
      },
      {
        path: 'categories/new',
        loadComponent: () =>
          import('./features/categories/categories-add-edit/categories-add-edit').then(
            (m) => m.CategoriesAddEditComponent,
          ),
        canActivate: [adminGuard],
      },
      {
        path: 'categories',
        loadComponent: () =>
          import('./features/categories/categories').then((m) => m.CategoriesComponent),
      },
      {
        path: 'users/:id/edit',
        loadComponent: () =>
          import('./features/users/user-edit/user-edit').then((m) => m.UserEditComponent),
        canActivate: [adminGuard],
      },
      {
        path: 'users',
        loadComponent: () => import('./features/users/users').then((m) => m.UsersComponent),
        canActivate: [adminGuard],
      },
      { path: '**', redirectTo: 'tasks' },
    ],
  },
];
