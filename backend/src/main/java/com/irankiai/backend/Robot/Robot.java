package com.irankiai.backend.Robot;

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
@Table(name = "robots")
public class Robot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "grid_id")
    private Grid location;
    
    @OneToOne
    @JoinColumn(name = "container_id")
    private Container container;
    
    private int batteryLevel;
    
    public Robot() {
    }
    
    public Robot(Grid location, int batteryLevel) {
        this.location = location;
        this.batteryLevel = batteryLevel;
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
    
    public int getBatteryLevel() {
        return batteryLevel;
    }
    
    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
    
    public boolean isCarryingContainer() {
        return container != null;
    }
}