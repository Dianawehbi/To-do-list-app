import { Component, computed, inject, input, output } from '@angular/core';
import { Task, TaskStatus } from '../../../models/task.model';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-task-card',
  imports: [],
  templateUrl: './task-card.html',
  styleUrl: './task-card.css',
})
export class TaskCardComponent {
  task = input.required<Task>();
  router = inject(Router);
  private authService = inject(AuthService);

  isAdmin = computed(() => this.authService.isAdmin());


  delete = output<number>();
  statusChange = output<{ id: number; status: TaskStatus }>();

  availableNextStatuses = computed<TaskStatus[]>(() => {
    const current = this.task().status;

    switch (current) {
      case 'TODO':
        return ['IN_PROGRESS'];
      case 'IN_PROGRESS':
        return ['TODO', 'DONE'];
      case 'DONE':
        return ['IN_PROGRESS']; // NOT 'TODO' — must go through IN_PROGRESS
      default:
        return [];
    }
  });

  onDelete(): void {
    this.delete.emit(this.task().id);
  }

  onEdit(): void {
   
    this.router.navigate(['tasks' , this.task().id , 'edit'])
  }

  onStatusChange(newStatus: TaskStatus): void {
    this.statusChange.emit({ id: this.task().id, status: newStatus });
  }
}
