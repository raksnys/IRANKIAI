import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
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
  selector: 'app-product',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product.component.html',
  styleUrl: './product.component.scss'
})
export class ProductComponent implements OnInit {
  product?: Product;
  loading = true;
  error = false;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchProduct(id);
    }
  }

  fetchProduct(id: string): void {
    this.http.get<Product>(`${environment.apiUrl}/product?id=${id}`)
      .subscribe({
        next: (data) => {
          this.product = data;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error fetching product:', error);
          this.loading = false;
          this.error = true;
        }
      });
  }

  goBack(): void {
    window.history.back();
  }
}