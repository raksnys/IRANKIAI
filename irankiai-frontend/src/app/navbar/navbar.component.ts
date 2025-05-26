import { Component, ViewEncapsulation } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { routes } from '../app.routes';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class NavbarComponent {
  constructor(public auth: AuthService) {}

  getUserDisplayName(): string {
    const userInfo = this.auth.getUserInfo();
    if (userInfo) {
      return `${userInfo.role.charAt(0).toUpperCase() + userInfo.role.slice(1)}`;
    }
    return '';
  }

  getAvailableLinks(): { path: string, label: string }[] {
    const userInfo = this.auth.getUserInfo();
    if (!userInfo) return [];

    const role = userInfo.role;
    const links: { path: string, label: string }[] = [];

    // Map of route paths to their display labels
    const routeLabels: { [key: string]: string } = {
      '/dashboard': 'Pradžia',
      '/admin': 'Administratorius', 
      '/buyer': 'Pirkėjas',
      '/merchant': 'Pardavėjas',
      '/catalog': 'Prekės',
      '/orders': 'Užsakymai',
      '/add-product': 'Pridėti prekę',
      '/telemetry': 'Telemetrija',
      '/cart': 'Krepšelis'
    };

    // Check each route for role-based access
    routes.forEach(route => {
      if (route.path && route.data && route.data['role'] && routeLabels[`/${route.path}`]) {
        const requiredRoles = Array.isArray(route.data['role']) 
          ? route.data['role'] 
          : [route.data['role']];

        if (requiredRoles.includes(role)) {
          links.push({
            path: route.path,
            label: routeLabels[`/${route.path}`]
          });
        }
      }
    });

    return links;
  }
}
