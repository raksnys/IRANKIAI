package com.irankiai.backend.DeliverOrder;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Grid.Grid;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "deliver_orders")
public class DeliverOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "grid_id")
    private Grid location;
    
    @OneToOne
    @JoinColumn(name = "container_id")
    private Container container;
    
    private Integer targetContainerIdForProductDelivery; // NEW FIELD
    
    public DeliverOrder() {
    }
    
    public DeliverOrder(Grid location) {
        this.location = location;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Grid getLocation() {
        return location;
    }
    
    public void setLocation(Grid location) {
        this.location = location;
    }
    
    public Container getContainer() {
        return container;
    }
    
    public void setContainer(Container container) {
        this.container = container;
    }
    
    public boolean hasContainer() {
        return container != null;
    }

    public Integer getTargetContainerIdForProductDelivery() {
        return targetContainerIdForProductDelivery;
    }

    public void setTargetContainerIdForProductDelivery(Integer targetContainerIdForProductDelivery) {
        this.targetContainerIdForProductDelivery = targetContainerIdForProductDelivery;
    }
}