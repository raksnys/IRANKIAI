import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Admin page</h2>
    <button (click)="auth.logout()">Logout</button>
  `
})
export class AdminComponent {
  constructor(public auth: AuthService) {}
} 