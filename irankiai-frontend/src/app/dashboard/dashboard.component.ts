import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container">
      <div class="welcome-message">
        <h1>Sveiki atvykę, {{ getUserDisplayName() }}!</h1>
        <p class="role">Jūsų rolė: {{ getUserRole() }}</p>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 80vh;
      padding: 2rem;
    }

    .welcome-message {
      text-align: center;
      background: white;
      padding: 3rem;
      border-radius: 12px;
      box-shadow: 0 2px 16px rgba(0,0,0,0.12);
    }

    h1 {
      color: #1976d2;
      font-size: 2.5rem;
      margin-bottom: 1rem;
    }

    .role {
      color: #666;
      font-size: 1.2rem;
    }
  `]
})
export class DashboardComponent {
  constructor(private auth: AuthService) {}

  getUserDisplayName(): string {
    const userInfo = this.auth.getUserInfo();
    if (userInfo) {
      return `${userInfo.role.charAt(0).toUpperCase() + userInfo.role.slice(1)}`;
    }
    return '';
  }

  getUserRole(): string {
    const userInfo = this.auth.getUserInfo();
    return userInfo ? userInfo.role : '';
  }
} 