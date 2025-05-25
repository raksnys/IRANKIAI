import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>User page</h2>
    <button (click)="auth.logout()">Logout</button>
  `
})
export class UserComponent {
  constructor(public auth: AuthService) {}
} 