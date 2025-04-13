import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { OrderService, Order } from '../services/order.service';

@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-confirmation.component.html',
  styleUrl: './order-confirmation.component.scss'
})
export class OrderConfirmationComponent implements OnInit {
  order: Order | null = null;
  loading = true;
  error = false;
  
  constructor(
    private orderService: OrderService,
    private route: ActivatedRoute
  ) {}
  
  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const orderId = Number(params.get('id'));
      
      if (!orderId) {
        this.error = true;
        this.loading = false;
        return;
      }
      
      this.orderService.getOrder(orderId).subscribe({
        next: (order) => {
          this.order = order;
          this.loading = false;
        },
        error: (err) => {
          console.error('Error loading order:', err);
          this.error = true;
          this.loading = false;
        }
      });
    });
  }
}