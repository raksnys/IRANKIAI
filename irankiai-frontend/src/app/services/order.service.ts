import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface OrderItem {
  id: number;
  product: {
    id: number;
    name: string;
    price: number;
    dimensions: string;
    weight: number;
    color: string;
  };
  quantity: number;
  price: number;
}

export interface Order {
  id: number;
  userId: number;
  items: OrderItem[];
  status: string;
  createdAt: string;
  completedAt: string | null;
  totalPrice: number;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  
  constructor(private http: HttpClient) {}
  
  checkout(): Observable<Order> {
    return this.http.post<Order>(`${environment.apiUrl}/cart/checkout`, {});
  }
  
  getOrder(orderId: number): Observable<Order> {
    return this.http.get<Order>(`${environment.apiUrl}/orders/${orderId}`);
  }
  
  getUserOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${environment.apiUrl}/orders/user`);
  }
}