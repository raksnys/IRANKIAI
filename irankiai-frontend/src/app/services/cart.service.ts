import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../environments/environment';

export interface Product {
  id: number;
  name: string;
  dimensions: string;
  weight: number;
  price: number;
  color: string;
}

export interface CartItem {
  id: number;
  product: Product;
  quantity: number;
}

export interface Cart {
  id: number;
  userId: number;
  items: CartItem[];
}

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private cartSubject = new BehaviorSubject<Cart | null>(null);
  cart$ = this.cartSubject.asObservable();
  
  constructor(private http: HttpClient) {
    this.loadCart();
  }
  
  loadCart(): void {
    this.http.get<Cart>(`${environment.apiUrl}/cart`)
      .subscribe({
        next: (cart) => {
          this.cartSubject.next(cart);
        },
        error: (err) => {
          console.error('Error loading cart:', err);
        }
      });
  }
  
  addToCart(productId: number, quantity: number = 1): Observable<Cart> {
    return this.http.put<Cart>(`${environment.apiUrl}/cart/items/${productId}?quantity=${quantity}`, {})
      .pipe(
        tap(cart => this.cartSubject.next(cart))
      );
  }
  
  updateQuantity(productId: number, quantity: number): Observable<Cart> {
    return this.http.put<Cart>(`${environment.apiUrl}/cart/items/${productId}?quantity=${quantity}`, {})
      .pipe(
        tap(cart => this.cartSubject.next(cart))
      );
  }
  
  removeItem(productId: number): Observable<Cart> {
    return this.http.delete<Cart>(`${environment.apiUrl}/cart/items/${productId}`)
      .pipe(
        tap(cart => this.cartSubject.next(cart))
      );
  }
  
  clearCart(): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/cart`)
      .pipe(
        tap(() => this.loadCart())
      );
  }
  
  getCartItemCount(): number {
    const cart = this.cartSubject.value;
    if (!cart || !cart.items) return 0;
    
    return cart.items.reduce((count, item) => count + item.quantity, 0);
  }
  
  getCartTotal(): number {
    const cart = this.cartSubject.value;
    if (!cart || !cart.items) return 0;
    
    return cart.items.reduce((total, item) => total + (item.product.price * item.quantity), 0);
  }
}