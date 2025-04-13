package com.irankiai.backend.Cart;

import com.irankiai.backend.Product.Product;
import com.irankiai.backend.Product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(userId);
                    return cartRepository.save(newCart);
                });
    }
    
    @Transactional
    public Cart addItemToCart(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        
        Product product = productRepository.findById(productId.intValue())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if the product is already in the cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId.intValue())
                .findFirst();
        
        if (existingItem.isPresent()) {
            // Update quantity if product already exists in cart
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            // Add new item to cart
            CartItem newItem = new CartItem(product, quantity);
            cart.addItem(newItem);
        }
        
        return cartRepository.save(cart);
    }
    
    @Transactional
    public Cart updateCartItemQuantity(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId.intValue())
                .findFirst()
                .ifPresent(item -> {
                    if (quantity <= 0) {
                        cart.removeItem(item);
                    } else {
                        item.setQuantity(quantity);
                    }
                });
        
        return cartRepository.save(cart);
    }
    
    @Transactional
    public Cart removeItemFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        
        cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId.intValue())
                .findFirst()
                .ifPresent(cart::removeItem);
        
        return cartRepository.save(cart);
    }
    
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}