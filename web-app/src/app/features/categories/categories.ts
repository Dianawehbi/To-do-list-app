import { Component, computed, inject, signal } from '@angular/core';
import { CategoryCard } from './category-card/category-card';

import { Category } from '../../models/category.model';
import { ApiErrorResponse } from '../../models/auth.model';
import { CategoryService } from '../../services/CategoryService';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-categories',
  imports: [CategoryCard, RouterLink, ReactiveFormsModule],
  templateUrl: './categories.html',
  styleUrl: './categories.css',
})
export class CategoriesComponent {
  private categoryService = inject(CategoryService);
  private authService = inject(AuthService);

  // GET    /api/categories?active=true
  // POST   /api/categories               admin only
  // GET    /api/categories/{id}
  // PUT    /api/categories/{id}          admin only
  // DELETE /api/categories/{id}          admin only

  categories = signal<Category[]>([]);
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  currentPage = signal(0);
  pageSize = signal(12);
  totalPages = signal(0);
  totalElements = signal(0);

  isAdmin = computed(() => this.authService.isAdmin());

  // filter
  activeFilter = signal<boolean | undefined>(undefined);

  statusFilter = new FormControl('all');

  constructor() {
    this.statusFilter.valueChanges.subscribe((value) => {
      if (value != null) {
        this.filterCategories(value);
      }
    });
  }

  ngOnInit() {
    this.loadCategories();
  }

  loadCategories() {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.categoryService
      .getAllCategories(this.currentPage(), this.pageSize(), this.activeFilter())
      .subscribe({
        next: (response) => {
          this.categories.set(response.content);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
          this.isLoading.set(false);
        },

        error: (error: ApiErrorResponse) => {
          this.errorMessage.set(error.message ?? 'Failed to load categories');

          this.isLoading.set(false);
        },
      });
  }

  filterCategories(value: String) {
    if (value === 'active') {
      this.activeFilter.set(true);
    } else if (value === 'inactive') {
      this.activeFilter.set(false);
    } else {
      this.activeFilter.set(undefined);
    }
    this.currentPage.set(0);
    this.loadCategories();
  }

  nextPage() {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update((page) => page + 1);
      this.loadCategories();
    }
  }

  previousPage() {
    if (this.currentPage() > 0) {
      this.currentPage.update((page) => page - 1);
      this.loadCategories();
    }
  }

  goToPage(page: number) {
    this.currentPage.set(page);

    this.loadCategories();
  }

  async onDeleteCategory(id: number) {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    try {
      await this.categoryService.deleteCategory(id);
      await this.loadCategories();
    } catch (error) {
      this.errorMessage.set((error as ApiErrorResponse).message);
    } finally {
      this.isLoading.set(false);
    }
  }
}
