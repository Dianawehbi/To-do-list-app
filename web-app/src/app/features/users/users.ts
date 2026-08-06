import { Component, DestroyRef, inject, signal } from '@angular/core';
import { UserCard } from './user-card/user-card';
import { User, UserRequestData } from '../../models/user.model';
import { ApiErrorResponse, PageResponse } from '../../models/auth.model';
import { UserService } from '../../services/UserService';
import { catchError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-users',
  imports: [UserCard],
  templateUrl: './users.html',
  styleUrl: './users.css',
})
export class UsersComponent {
  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);

  users = signal<User[]>([]);

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  // pagination
  currentPage = signal(0);
  pageSize = signal(10);

  totalPages = signal(0);
  totalElements = signal(0);

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.userService.getAllUsers(this.currentPage(), this.pageSize()).subscribe({
      next: (response: PageResponse<User>) => {
        this.users.set(response.content);

        this.totalPages.set(response.totalPages);
        this.totalElements.set(response.totalElements);

        this.isLoading.set(false);
      },
      error: (error: ApiErrorResponse) => {
        this.errorMessage.set(error.message ?? 'Failed to load users');

        this.isLoading.set(false);
      },
    });
  }

  nextPage() {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update((page) => page + 1);

      this.loadUsers();
    }
  }

  previousPage() {
    if (this.currentPage() > 0) {
      this.currentPage.update((page) => page - 1);

      this.loadUsers();
    }
  }

  goToPage(page: number) {
    this.currentPage.set(page);
    this.loadUsers();
  }

  async onDelete(id: number): Promise<void> {
    console.log('delete method : ' + id);
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      await this.userService.deleteUser(id);
      console.log('delete method next: ');
      await this.loadUsers(); // refresh only after delete actually succeeds
    } catch (error) {
      console.log('delete method error: ');
      this.errorMessage.set((error as ApiErrorResponse).message);
      console.log("done on delete");
    } finally {
      this.isLoading.set(false);
    }
  }
  
  async onEnable(user: { id: number; data: UserRequestData }): Promise<void> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      await this.userService.updateUser(user.id, user.data);
      console.log('update method next: ');
      await this.loadUsers(); // refresh only after delete actually succeeds
    } catch (error) {
      console.log('enable method error: ');
      this.errorMessage.set((error as ApiErrorResponse).message);
    } finally {
      this.isLoading.set(false);
    }
  }
}
