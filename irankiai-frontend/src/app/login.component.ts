import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h2>Login</h2>
        <form (ngSubmit)="onLogin()">
          <input [(ngModel)]="username" name="username" placeholder="Username" required />
          <input [(ngModel)]="password" name="password" type="password" placeholder="Password" required />
          <button type="submit">Login</button>
        </form>
        <div *ngIf="error" class="error">{{error}}</div>
        <div class="register-link">
          <span>Don't have an account?</span>
          <button type="button" (click)="goToRegister()">Register</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 80vh;
    }
    .login-card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 2px 16px rgba(0,0,0,0.12);
      padding: 2rem 2.5rem;
      min-width: 320px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    h2 {
      margin-bottom: 1.5rem;
    }
    form {
      display: flex;
      flex-direction: column;
      width: 100%;
      gap: 1rem;
    }
    input {
      padding: 0.5rem 1rem;
      border: 1px solid #bbb;
      border-radius: 6px;
      font-size: 1rem;
    }
    button[type="submit"] {
      background: #222;
      color: #fff;
      border: none;
      border-radius: 6px;
      padding: 0.6rem 0;
      font-size: 1.1rem;
      margin-top: 0.5rem;
      cursor: pointer;
      transition: background 0.2s;
    }
    button[type="submit"]:hover {
      background: #444;
    }
    .register-link {
      margin-top: 1.5rem;
      display: flex;
      flex-direction: row;
      align-items: center;
      gap: 0.5rem;
    }
    .register-link button {
      background: #1976d2;
      color: #fff;
      border: none;
      border-radius: 6px;
      padding: 0.4rem 1rem;
      font-size: 1rem;
      cursor: pointer;
      transition: background 0.2s;
    }
    .register-link button:hover {
      background: #125ea2;
    }
    .error {
      color: #d32f2f;
      margin-top: 1rem;
      font-weight: 500;
    }
  `]
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';

  constructor(private auth: AuthService, private router: Router) {}

  onLogin() {
    this.error = '';
    this.auth.login(this.username, this.password).subscribe(res => {
      if (res.startsWith('error')) {
        this.error = 'Invalid username or password';
      } else {
        // redirect pagal role
        const info = this.auth.getUserInfo();
        if (info) {
          if (info.role === 'admin') this.router.navigate(['/admin']);
          else if (info.role === 'buyer') this.router.navigate(['/buyer']);
          else if (info.role === 'merchant') this.router.navigate(['/merchant']);
          else this.router.navigate(['/user']);
        }
      }
    });
  }

  goToRegister() {
    this.router.navigate(['/register']);
  }
} 