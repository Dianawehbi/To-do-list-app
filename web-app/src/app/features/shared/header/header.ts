import { Component, computed, inject, input, output } from '@angular/core';
import { User } from '../../../models/user.model';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class HeaderComponent {
  authService = inject(AuthService);

  currentUser = this.authService.currentUser;

  isAdmin = computed(() => this.currentUser()?.role == 'ADMIN');
  isLoggedIN = computed(() => this.currentUser());

  menuOpen = false;

  onLogout(): void {
    this.authService.logout();
  }
}
