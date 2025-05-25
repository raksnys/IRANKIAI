import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-buyer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Buyer page</h2>
    <button (click)="auth.logout()">Logout</button>
  `
})
export class BuyerComponent {
  constructor(public auth: AuthService) {}
} 