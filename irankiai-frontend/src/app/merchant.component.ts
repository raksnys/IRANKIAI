import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-merchant',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Merchant page</h2>
    <button (click)="auth.logout()">Logout</button>
  `
})
export class MerchantComponent {
  constructor(public auth: AuthService) {}
} 