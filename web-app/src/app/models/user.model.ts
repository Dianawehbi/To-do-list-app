export type UserRole = 'USER' | 'ADMIN';

export interface UserResponseData {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  enabled: boolean;
}

export interface UserRequestData {
  username: string;
  email: string;
  role: UserRole;
  enabled: boolean;
}

export class User {
  public id: number;
  public username: string;
  public email: string;
  public role: UserRole;
  public enabled: boolean;

  constructor(data: {
    id: number;
    username: string;
    email: string;
    role: UserRole;
    enabled: boolean;
  }) {
    this.id = data.id;
    this.username = data.username;
    this.email = data.email;
    this.role = data.role;
    this.enabled = data.enabled;
  }

  get isAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  get isActive(): boolean {
    return this.enabled;
  }
}