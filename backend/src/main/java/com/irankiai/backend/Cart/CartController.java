package com.irankiai.backend.Cart;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.irankiai.backend.Order.Order;
import com.irankiai.backend.Order.OrderService;
import com.irankiai.backend.Task.Task;
import com.irankiai.backend.Task.TaskQueryService;
import com.irankiai.backend.Task.TaskRepository;
import com.irankiai.backend.Task.TaskStatus;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;
    private final TaskRepository taskRepository;
    private final TaskQueryService taskQueryService;

    public Long DEMO_USER_ID = 1L;

    @Autowired
    public CartController(CartService cartService, OrderService orderService,
            TaskRepository taskRepository, TaskQueryService taskQueryService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.taskRepository = taskRepository;
        this.taskQueryService = taskQueryService;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        return ResponseEntity.ok(cartService.getOrCreateCart(DEMO_USER_ID));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addToCart(@RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        return ResponseEntity.ok(cartService.addItemToCart(DEMO_USER_ID, productId, quantity));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable Long productId, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(DEMO_USER_ID, productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(DEMO_USER_ID, productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart(DEMO_USER_ID);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = getUserIdFromAuthHeader(authHeader);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Authentication required for checkout");
            }

            Order order = orderService.createOrderFromCart(userId);

            // Use the service method instead of the repository directly
            Optional<Task> taskOpt = taskQueryService.findTaskByOrder(order);

            if (taskOpt.isPresent() && taskOpt.get().getStatus() == TaskStatus.WAITING_FOR_INVENTORY) {
                Task task = taskOpt.get();
                // Return a special response for orders waiting for inventory
                Map<String, Object> response = new HashMap<>();
                response.put("order", order);
                response.put("status", "WAITING_FOR_INVENTORY");
                response.put("missingProducts", task.getMissingProducts());

                return ResponseEntity.accepted().body(response);
            }

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String stacktraceString = sw.toString();

            // Return full stacktrace in the response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Checkout failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("stacktrace", stacktraceString);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Extract user ID from authentication header
     * In a real implementation, this would validate JWT tokens or other auth
     * mechanisms
     */
    private Long getUserIdFromAuthHeader(String authHeader) {
        // Placeholder implementation
        // In production, you would:
        // 1. Validate the token (JWT or other)
        // 2. Extract the user ID or username
        // 3. Return the user ID

        // Example JWT parsing (you would need a proper JWT library):
        // if (authHeader != null && authHeader.startsWith("Bearer ")) {
        // String token = authHeader.substring(7);
        // Claims claims =
        // Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        // return Long.parseLong(claims.getSubject());
        // }

        // For testing/development - replace with actual auth in production
        return 1L; // Default test user
    }

    // Add a logger for proper error tracking
}