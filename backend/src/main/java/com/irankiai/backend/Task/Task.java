package com.irankiai.backend.Task;

import com.irankiai.backend.DeliverOrder.DeliverOrder;
import com.irankiai.backend.Order.Order;
import com.irankiai.backend.Path.Path;
import com.irankiai.backend.Product.Product;
import com.irankiai.backend.Robot.Robot;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "robot_id")
    private Robot assignedRobot;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToOne
    @JoinColumn(name = "deliver_order_id")
    private DeliverOrder deliverOrder;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<TaskItem> items = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "path_id")
    private Path path;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.CREATED;

    @ElementCollection
    @CollectionTable(name = "missing_products", joinColumns = @JoinColumn(name = "task_id"))
    private Map<Integer, Integer> missingProducts = new HashMap<>(); // Map of product ID to quantity needed

    // Constructors
    public Task() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Robot getAssignedRobot() {
        return assignedRobot;
    }

    public void setAssignedRobot(Robot robot) {
        this.assignedRobot = robot;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public DeliverOrder getDeliverOrder() {
        return deliverOrder;
    }

    public void setDeliverOrder(DeliverOrder deliverOrder) {
        this.deliverOrder = deliverOrder;
    }

    public List<TaskItem> getItems() {
        return items;
    }

    public void setItems(List<TaskItem> items) {
        this.items = items;
    }

    public void addItem(TaskItem item) {
        items.add(item);
        item.setTask(this);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public boolean isAllItemsCollected() {
        return items.stream().allMatch(TaskItem::isCollected);
    }

    // Update getter and setter
    public Map<Integer, Integer> getMissingProducts() {
        return missingProducts;
    }

    public void setMissingProducts(Map<Integer, Integer> missingProducts) {
        this.missingProducts = missingProducts;
    }

    // Update helper method
    public void addMissingProduct(Product product, int quantity) {
        missingProducts.put(product.getId(), missingProducts.getOrDefault(product.getId(), 0) + quantity);
    }

    // Helper method to check if waiting for inventory
    public boolean isWaitingForInventory() {
        return !missingProducts.isEmpty();
    }
}