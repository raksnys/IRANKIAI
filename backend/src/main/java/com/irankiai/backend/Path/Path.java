package com.irankiai.backend.Path;

import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Robot.Robot;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Path {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "robot_id")
    private Robot robot;
    
    @ElementCollection
    @OrderColumn
    @CollectionTable(name = "path_waypoints", joinColumns = @JoinColumn(name = "path_id"))
    private List<Grid> waypoints = new ArrayList<>();
    
    private boolean completed = false;
    
    private int currentWaypointIndex = 0;
    
    public Path() {}
    
    public Path(Robot robot, List<Grid> waypoints) {
        this.robot = robot;
        this.waypoints = waypoints;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Robot getRobot() {
        return robot;
    }
    
    public void setRobot(Robot robot) {
        this.robot = robot;
    }
    
    public List<Grid> getWaypoints() {
        return waypoints;
    }
    
    public void setWaypoints(List<Grid> waypoints) {
        this.waypoints = waypoints;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public int getCurrentWaypointIndex() {
        return currentWaypointIndex;
    }
    
    public void setCurrentWaypointIndex(int currentWaypointIndex) {
        this.currentWaypointIndex = currentWaypointIndex;
    }
    
    public Grid getNextWaypoint() {
        if (currentWaypointIndex < waypoints.size()) {
            return waypoints.get(currentWaypointIndex);
        }
        return null;
    }
    
    public void advanceToNextWaypoint() {
        currentWaypointIndex++;
        if (currentWaypointIndex >= waypoints.size()) {
            completed = true;
        }
    }
}