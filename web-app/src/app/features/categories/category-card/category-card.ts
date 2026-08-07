import { Component, computed, inject, input, OnInit, output } from '@angular/core';
import { Category } from '../../../models/category.model';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-category-card',
  imports: [],
  templateUrl: './category-card.html',
  styleUrl: './category-card.css',
})
export class CategoryCard implements OnInit {
  router = inject(Router);
  authService = inject(AuthService);

  isAdmin = computed(() => this.authService.isAdmin());
  category = input.required<Category>();

  delete = output<number>();

  ngOnInit(): void {
    console.log('category Cards');
    console.log(this.isAdmin());
  }

  onEdit(): void {
    this.router.navigate(['categories', this.category().id, 'edit']);
  }

  onDelete(): void {
    this.delete.emit(this.category().id);
  }
}
