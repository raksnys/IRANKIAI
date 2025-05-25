package com.irankiai.backend.Task;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Product.Product;
import jakarta.persistence.*;

@Entity
public class TaskItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "container_id") // This is the source container for this item
    private Container sourceContainer;
    
    private int quantity;
    
    private boolean collected = false; // Field to track if this item has been collected
    
    // Constructors
    public TaskItem() {}
    
    public TaskItem(Product product, Container container, int quantity) {
        this.product = product;
        this.sourceContainer = container; // Set the source container
        this.quantity = quantity;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Task getTask() {
        return task;
    }
    
    public void setTask(Task task) {
        this.task = task;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public Container getSourceContainer() {
        return sourceContainer;
    }
    
    public void setSourceContainer(Container container) {
        this.sourceContainer = container;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isCollected() {
        return collected;
    }

    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}