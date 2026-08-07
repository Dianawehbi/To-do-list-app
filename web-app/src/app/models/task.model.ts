import { Category } from './category.model';

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface TaskRequestData {
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;   
  categoryId: number | null;
}

export interface TaskResponseData {
  id: number;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;   
  categoryId: number | null;
  ownerUserId: number;
  createdAt: string;
  updatedAt: string;
}

export class Task {
  public id: number;
  public title: string;
  public description: string | null;
  public status: TaskStatus;
  public priority: TaskPriority;
  public dueDate: Date | null;
  public categoryId: number | null;
  public ownerUserId: number;
  public createdAt: Date;
  public updatedAt: Date;

  constructor(data: {
    id: number;
    title: string;
    description: string | null;
    status: TaskStatus;
    priority: TaskPriority;
    dueDate: Date | null;
    categoryId: number | null;
    ownerUserId: number;
    createdAt: Date;
    updatedAt: Date;
  }) {
    this.id = data.id;
    this.title = data.title;
    this.description = data.description;
    this.status = data.status;
    this.priority = data.priority;
    this.dueDate = data.dueDate;
    this.categoryId = data.categoryId;
    this.ownerUserId = data.ownerUserId;
    this.createdAt = data.createdAt;
    this.updatedAt = data.updatedAt;
  }

  get isDone(): boolean {
    return this.status === 'DONE';
  }

  get isOverdue(): boolean {
    if (!this.dueDate || this.isDone) return false;
    return this.dueDate < new Date();
  }
}