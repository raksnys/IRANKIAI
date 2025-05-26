import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard {
  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    // Check if user is authenticated
    if (!this.auth.isAuthenticated()) {
      this.router.navigate(['/login']);
      return false;
    }

    // Get required roles from route data
    const requiredRoles = route.data['role'];
    
    // If no roles are specified, allow access
    if (!requiredRoles) {
      return true;
    }

    // Check if user has required role
    const hasRequiredRole = this.auth.hasRole(requiredRoles);
    if (!hasRequiredRole) {
      // Redirect to appropriate page based on user's role
      const userInfo = this.auth.getUserInfo();
      if (userInfo) {
        this.router.navigate([`/${userInfo.role}`]);
      } else {
        this.router.navigate(['/login']);
      }
      return false;
    }

    return true;
  }
} 