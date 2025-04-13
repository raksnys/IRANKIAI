import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { environment } from '../environments/environment';

interface Product {
  id: number;
  name: string;
  dimensions: string;
  weight: number;
  price: number;
  color: string;
}

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.scss'
})
export class CatalogComponent implements OnInit {
  products: Product[] = [];
  loading = true;
  error = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.fetchProducts();
  }

  fetchProducts(): void {
    // Note: You may need to create this endpoint in the backend
    this.http.get<Product[]>(`${environment.apiUrl}/products`)
      .subscribe({
        next: (data) => {
          this.products = data;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error fetching products:', err);
          this.error = true;
          this.loading = false;
        }
      });
  }
}