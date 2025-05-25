import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="register-container">
      <div class="register-card">
        <h2>Register</h2>
        <form (ngSubmit)="onRegister()">
          <input [(ngModel)]="username" name="username" placeholder="Username" required />
          <input [(ngModel)]="password" name="password" type="password" placeholder="Password" required />
          <select [(ngModel)]="role" name="role" required>
            <option value="user">User</option>
            <option value="admin">Admin</option>
            <option value="buyer">Buyer</option>
            <option value="merchant">Merchant</option>
          </select>
          <button type="submit" class="register-btn">Register</button>
        </form>
        <div *ngIf="error" class="error">{{error}}</div>
        <div *ngIf="success" class="success">Registration successful! <button (click)="goToLogin()">Go to login</button></div>
        <button class="back-btn" (click)="goToLogin()">Back to login</button>
      </div>
    </div>
  `,
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 80vh;
    }
    .register-card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 2px 16px rgba(0,0,0,0.12);
      padding: 2rem 2.5rem;
      min-width: 340px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    h2 {
      margin-bottom: 1.5rem;
    }
    form {
      display: flex;
      flex-direction: row;
      gap: 0.5rem;
      width: 100%;
      margin-bottom: 1.5rem;
    }
    input, select {
      padding: 0.5rem 1rem;
      border: 1px solid #bbb;
      border-radius: 6px;
      font-size: 1rem;
    }
    .register-btn {
      background: #0a6c23;
      color: #fff;
      border: none;
      border-radius: 8px;
      padding: 0.6rem 2.2rem;
      font-size: 1.1rem;
      font-weight: bold;
      cursor: pointer;
      transition: background 0.2s;
      margin-left: 1rem;
    }
    .register-btn:hover {
      background: #09541a;
    }
    .back-btn {
      margin-top: 1.2rem;
      background: #eee;
      color: #222;
      border: none;
      border-radius: 6px;
      padding: 0.4rem 1.2rem;
      font-size: 1rem;
      cursor: pointer;
      transition: background 0.2s;
    }
    .back-btn:hover {
      background: #ccc;
    }
    .error {
      color: #d32f2f;
      margin-top: 1rem;
      font-weight: 500;
    }
    .success {
      color: #0a6c23;
      margin-top: 1rem;
      font-weight: 500;
    }
  `]
})
export class RegisterComponent {
  username = '';
  password = '';
  role = 'user';
  error = '';
  success = false;

  constructor(private auth: AuthService, private router: Router) {}

  onRegister() {
    this.error = '';
    this.success = false;
    this.auth.register(this.username, this.password, this.role).subscribe(res => {
      if (res === 'success') {
        this.success = true;
      } else {
        this.error = res.replace('error: ', '');
      }
    });
  }

  goToLogin() {
    this.router.navigate(['/login']);
  }
} 