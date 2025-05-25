package com.irankiai.backend.Path;

import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Robot.Robot;
import com.irankiai.backend.Task.Task; // Import Task
import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Path {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "robot_id")
    private Robot robot;

    @ElementCollection(fetch = FetchType.EAGER) 
    @CollectionTable(name = "path_waypoints", joinColumns = @JoinColumn(name = "path_id"))
    @OrderColumn 
    private List<Grid> waypoints = new ArrayList<>();

    @OneToOne(mappedBy = "path") 
    private Task task; 

    private boolean completed = false; 
    
    private int currentWaypointIndex = 0; // Add this field, initialize to 0 (start of path)

    public Path() {
        this.currentWaypointIndex = 0; // Default for new paths created via default constructor
        this.completed = false;
    }

    public Path(Robot robot, List<Grid> waypoints) {
        this.robot = robot;
        this.waypoints = waypoints;
        this.completed = false; 
        this.currentWaypointIndex = 0; // New paths start at the first waypoint
    }

    // Getters and Setters
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
 
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isCompleted() { 
        return completed;
    }

    public void setCompleted(boolean completed) { 
        this.completed = completed;
    }

    public int getCurrentWaypointIndex() { // Getter for the new field
        return currentWaypointIndex;
    }

    public void setCurrentWaypointIndex(int currentWaypointIndex) { // Setter for the new field
        this.currentWaypointIndex = currentWaypointIndex;
    }
}