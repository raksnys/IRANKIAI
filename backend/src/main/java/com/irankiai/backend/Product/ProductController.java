package com.irankiai.backend.Product;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class ProductController {

    private ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("product")
    public Product getProduct(@RequestParam Integer id) {
        Optional<Product> product = productService.getProduct(id);
        return product.orElse(null);
    }

    @PostMapping("product")
    public Product addProduct(@RequestBody Product product) {

        return productService.addProduct(product);
    }

    @GetMapping("products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

}
