package com.irankiai.backend.Order;

import com.irankiai.backend.Cart.Cart;
import com.irankiai.backend.Cart.CartItem;
import com.irankiai.backend.Cart.CartRepository;
import com.irankiai.backend.Task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private TaskService taskService;

    @Transactional
    public Order createOrderFromCart(Long userId) {
        try {
            // Find user's cart
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

            if (cart.getItems().isEmpty()) {
                throw new RuntimeException("Cannot create order from empty cart");
            }

            // Create new order
            Order order = new Order(userId);

            // Convert cart items to order items
            for (CartItem cartItem : cart.getItems()) {
                OrderItem orderItem = new OrderItem(
                        cartItem.getProduct(),
                        cartItem.getQuantity());
                order.addItem(orderItem);
            }

            // Calculate total price
            order.calculateTotalPrice();

            // Save order
            order = orderRepository.save(order);

            try {
                // Create task for order - this is where the grid creation might be happening
                taskService.createTaskForOrder(order);
            } catch (Exception e) {
                System.err.println("Warning: Failed to create task for order: " + e.getMessage());
                e.printStackTrace();
            }

            // Update order status
            order.setStatus(OrderStatus.PROCESSING);
            order = orderRepository.save(order);

            // Clear the cart
            cart.getItems().clear();
            cartRepository.save(cart);

            return order;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }
}