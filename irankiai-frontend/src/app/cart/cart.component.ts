import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { CartService, CartItem } from '../services/cart.service';
import { OrderService } from '../services/order.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.scss'
})
export class CartComponent implements OnInit {
  cartItems: CartItem[] = [];
  loading = true;
  processingCheckout = false;
  
  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private router: Router
  ) {}
  
  ngOnInit(): void {
    this.cartService.cart$.subscribe(cart => {
      this.cartItems = cart?.items || [];
      this.loading = false;
    });
  }
  
  updateQuantity(productId: number, quantity: number): void {
    this.cartService.updateQuantity(productId, quantity).subscribe();
  }
  
  removeItem(productId: number): void {
    this.cartService.removeItem(productId).subscribe();
  }
  
  clearCart(): void {
    this.cartService.clearCart().subscribe();
  }
  
  getTotal(): number {
    return this.cartService.getCartTotal();
  }
  
  checkout(): void {
    if (this.cartItems.length === 0) {
      return;
    }
    
    this.processingCheckout = true;
    
    this.orderService.checkout().subscribe({
      next: (order) => {
        this.processingCheckout = false;
        this.router.navigate(['/order-confirmation', order.id]);
      },
      error: (err) => {
        console.error('Checkout failed:', err);
        this.processingCheckout = false;
        // You could add an error message display here
      }
    });
  }
}