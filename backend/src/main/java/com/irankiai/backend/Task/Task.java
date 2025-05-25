package com.irankiai.backend.Task;

import com.irankiai.backend.CollectOrder.CollectOrder;
import com.irankiai.backend.DeliverOrder.DeliverOrder; // Keep for other task types if needed
import com.irankiai.backend.Grid.Grid; // Import Grid
import com.irankiai.backend.Order.Order;
import com.irankiai.backend.Path.Path;
import com.irankiai.backend.Product.Product;
import com.irankiai.backend.Robot.Robot;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assigned_robot_id")
    private Robot assignedRobot;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskItem> items = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "path_id", referencedColumnName = "id")
    private Path path;

    @ManyToOne // A task belongs to an order (e.g. for full order fulfillment)
    @JoinColumn(name = "order_id")
    private Order order;

    // This DeliverOrder might be used for complex order fulfillment,
    // but not for direct product-to-container delivery as per new logic.
    @ManyToOne(cascade = CascadeType.PERSIST) 
    @JoinColumn(name = "deliver_order_id")
    private DeliverOrder deliverOrder; 

    @ManyToOne(cascade = CascadeType.PERSIST) 
    @JoinColumn(name = "collect_order_id") 
    private CollectOrder collectOrder; 

    // New fields for direct product delivery to a container
    private Integer productDeliveryTargetContainerId;

    @ManyToOne
    @JoinColumn(name = "product_delivery_target_location_id")
    private Grid productDeliveryTargetLocation;


    public Task() {
    }

    // Getters and Setters for new fields
    public Integer getProductDeliveryTargetContainerId() {
        return productDeliveryTargetContainerId;
    }

    public void setProductDeliveryTargetContainerId(Integer productDeliveryTargetContainerId) {
        this.productDeliveryTargetContainerId = productDeliveryTargetContainerId;
    }

    public Grid getProductDeliveryTargetLocation() {
        return productDeliveryTargetLocation;
    }

    public void setProductDeliveryTargetLocation(Grid productDeliveryTargetLocation) {
        this.productDeliveryTargetLocation = productDeliveryTargetLocation;
    }

    // ... existing getters and setters ...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Robot getAssignedRobot() {
        return assignedRobot;
    }

    public void setAssignedRobot(Robot assignedRobot) {
        this.assignedRobot = assignedRobot;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public List<TaskItem> getItems() {
        return items;
    }

    public void setItems(List<TaskItem> items) {
        this.items = items;
        if (this.items != null) {
            this.items.forEach(item -> item.setTask(this)); 
        }
    }

    public void addItem(TaskItem item) {
        this.items.add(item);
        item.setTask(this);
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
        if (this.path != null) { // Ensure bidirectional link if Path has setTask
            this.path.setTask(this);
        }
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

    public CollectOrder getCollectOrder() {
        return collectOrder;
    }

    public void setCollectOrder(CollectOrder collectOrder) {
        this.collectOrder = collectOrder;
    }

    public List<Product> getMissingProducts() {
        if (this.items == null) {
            return new ArrayList<>();
        }
        return this.items.stream()
                .filter(item -> !item.isCollected()) 
                .map(TaskItem::getProduct)
                .distinct() 
                .collect(Collectors.toList());
    }
}