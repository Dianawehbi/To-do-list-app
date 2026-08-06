import { Component, inject, input, output } from '@angular/core';
import { Router } from '@angular/router';
import { User, UserRequestData, UserRole } from '../../../models/user.model';

@Component({
  selector: 'app-user-card',
  imports: [],
  templateUrl: './user-card.html',
  styleUrl: './user-card.css',
})
export class UserCard {
  private router = inject(Router);

  user = input.required<User>();

  enable = output<{ id: number; data: UserRequestData }>();
  disable = output<number>();

  onToggleEnabled(): void {
    if (this.user().enabled) {
      this.disable.emit(this.user().id);
    } else {
      const data: UserRequestData = {
        username: this.user().username,
        email: this.user().email,
        role: this.user().role,
        enabled: true,
      };
      this.enable.emit({ id: this.user().id, data: data });
    }
  }

  onEdit(): void {
    this.router.navigate(['/users', this.user().id, 'edit']);
  }
}
