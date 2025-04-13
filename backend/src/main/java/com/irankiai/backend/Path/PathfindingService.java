package com.irankiai.backend.Path;

import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Grid.GridRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PathfindingService {

    // Add the missing repository dependency
    @Autowired
    private GridRepository gridRepository;

    // A* pathfinding algorithm to find path between two points
    public List<Grid> findPath(Grid start, Grid end) {
        // This is a simplified implementation
        // A full A* implementation would consider obstacles and efficiency

        if (start == null || end == null) {
            return new ArrayList<>();
        }

        List<Grid> path = new ArrayList<>();
        path.add(new Grid(start.getX(), start.getY(), start.getZ()));

        // If start and end are the same, return just the start position
        if (start.getX() == end.getX() && start.getY() == end.getY() && start.getZ() == end.getZ()) {
            return path;
        }

        // Simple direct path (move in X, then Y, then Z)
        // X movement
        int currentX = start.getX();
        while (currentX != end.getX()) {
            if (currentX < end.getX()) {
                currentX++;
            } else {
                currentX--;
            }
            path.add(new Grid(currentX, start.getY(), start.getZ()));
        }

        // Y movement
        int currentY = start.getY();
        while (currentY != end.getY()) {
            if (currentY < end.getY()) {
                currentY++;
            } else {
                currentY--;
            }
            path.add(new Grid(end.getX(), currentY, start.getZ()));
        }

        // Z movement
        int currentZ = start.getZ();
        while (currentZ != end.getZ()) {
            if (currentZ < end.getZ()) {
                currentZ++;
            } else {
                currentZ--;
            }
            path.add(new Grid(end.getX(), end.getY(), currentZ));
        }

        // Fix the pathGrids variable name - use path instead
        List<Grid> persistedGrids = new ArrayList<>();
        for (Grid waypoint : path) {
            // Fix the null check
            if (waypoint.getId() == null || waypoint.getId() <= 0) {
                persistedGrids.add(gridRepository.save(waypoint));
            } else {
                persistedGrids.add(waypoint);
            }
        }

        return persistedGrids;
    }
}