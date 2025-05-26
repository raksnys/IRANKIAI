import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:777/api/api';
  private storageKey = 'userInfo'; // saugosim id:role

  constructor(private http: HttpClient, private router: Router) {}

  login(username: string, password: string): Observable<string> {
    return this.http.post(this.apiUrl + '/login', { username, password }, { responseType: 'text' }).pipe(
      tap(res => {
        if (!res.startsWith('error')) {
          localStorage.setItem(this.storageKey, res);
        }
      })
    );
  }

  register(username: string, password: string, role: string): Observable<string> {
    return this.http.post(this.apiUrl + '/register', { username, password, role }, { responseType: 'text' });
  }

  logout() {
    localStorage.removeItem(this.storageKey);
    this.router.navigate(['/login']);
  }

  getUserInfo(): { id: string, role: string } | null {
    const info = localStorage.getItem(this.storageKey);
    if (info && info.includes(':')) {
      const [id, role] = info.split(':');
      return { id, role };
    }
    return null;
  }

  isLoggedIn(): boolean {
    return !!this.getUserInfo();
  }

  isAuthenticated(): boolean {
    return this.isLoggedIn();
  }

  hasRole(requiredRoles: string | string[]): boolean {
    const userInfo = this.getUserInfo();
    if (!userInfo) return false;

    if (Array.isArray(requiredRoles)) {
      return requiredRoles.includes(userInfo.role);
    }
    return userInfo.role === requiredRoles;
  }
} 