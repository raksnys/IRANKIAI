package com.irankiai.backend.Path;

import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Grid.GridRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PathfindingService {

    @Autowired
    private GridRepository gridRepository;

    // Define map boundaries (consider moving to a central config or service)
    private static final int MAX_X = 48; // As seen in RobotMovementService
    private static final int MAX_Y = 15; // As seen in RobotMovementService
    private static final int MAX_Z = 10; // As seen in RobotMovementService

    private static class Node {
        Grid grid;
        Node parent;
        double gCost; // Cost from start to current node
        double hCost; // Heuristic cost from current node to end
        double fCost; // gCost + hCost

        Node(Grid grid, Node parent, double gCost, double hCost) {
            this.grid = grid;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(grid, node.grid);
        }

        @Override
        public int hashCode() {
            return Objects.hash(grid);
        }
    }

    /**
     * Finds a path to an interaction point (adjacent cell) near the target object.
     *
     * @param start           The starting grid cell.
     * @param objectLocation  The actual grid cell of the target object.
     * @param occupiedCells   A list of grid cells that are considered obstacles.
     * @return A list of grid cells representing the path, or an empty list if no path found.
     */
    public List<Grid> findPathToInteract(Grid start, Grid objectLocation, List<Grid> occupiedCellsInput) {
        System.out.println("PS.findPathToInteract: Start: " + start.getX()+","+start.getY()+","+start.getZ() + " (ID:"+start.getId()+")"
                         + ", ObjectLocation: " + objectLocation.getX()+","+objectLocation.getY()+","+objectLocation.getZ() + " (ID:"+objectLocation.getId()+")");
        System.out.println("PS.findPathToInteract: Received occupiedCellsInput: " +
            occupiedCellsInput.stream().map(g -> "GridID " + g.getId() + " (" + g.getX()+","+g.getY()+","+g.getZ() + ")").collect(Collectors.joining("; ")));

        if (start == null || objectLocation == null) {
            System.err.println("PathfindingService: Start or objectLocation is null for findPathToInteract.");
            return new ArrayList<>();
        }

        Grid startGrid = getOrCreateGrid(start.getX(), start.getY(), start.getZ());
        Grid objectGrid = getOrCreateGrid(objectLocation.getX(), objectLocation.getY(), objectLocation.getZ());

        List<Grid> potentialInteractionSpots = getAdjacentUnoccupiedCells(objectGrid, occupiedCellsInput, startGrid);
        System.out.println("PS.findPathToInteract: PotentialInteractionSpots: " +
            potentialInteractionSpots.stream().map(g -> "GridID " + g.getId() + " (" + g.getX()+","+g.getY()+","+g.getZ() + ")").collect(Collectors.joining("; ")));

        if (potentialInteractionSpots.isEmpty()) {
            System.err.println("PathfindingService: No valid interaction spot found near object at " + objectGrid.getX() + "," + objectGrid.getY() + "," + objectGrid.getZ());
            return new ArrayList<>();
        }

        // Sort spots by distance to start, to try pathfinding to the closest one first
        potentialInteractionSpots.sort(Comparator.comparingDouble(spot -> calculateManhattanDistance(startGrid, spot)));

        for (Grid targetSpot : potentialInteractionSpots) {
            System.out.println("PS.findPathToInteract: Attempting to find path to targetSpot: " + targetSpot.getX()+","+targetSpot.getY()+","+targetSpot.getZ() + " (ID:"+targetSpot.getId()+")");
            List<Grid> pathToSpot = findPath(startGrid, targetSpot, occupiedCellsInput, false); // false: targetSpot itself is not an obstacle
            if (!pathToSpot.isEmpty()) {
                System.out.println("PathfindingService: Path found to interaction spot " + targetSpot.getX() + "," + targetSpot.getY() + "," + targetSpot.getZ() +
                                   " for object at " + objectGrid.getX() + "," + objectGrid.getY() + "," + objectGrid.getZ());
                return pathToSpot;
            }
        }
        System.err.println("PathfindingService: Could not find a path to any valid interaction spots near " + objectGrid.getX() + "," + objectGrid.getY() + "," + objectGrid.getZ());
        return new ArrayList<>();
    }


    /**
     * A* pathfinding algorithm to find path between two points, considering obstacles.
     *
     * @param startGrid         The starting grid cell.
     * @param endGrid           The target grid cell.
     * @param occupiedCells     A list of grid cells that are considered obstacles.
     * @param endIsObstacle     If true, the endGrid itself is considered an obstacle (e.g. pathing through it).
     *                          If false, the endGrid is the destination and not an obstacle for itself.
     * @return A list of grid cells representing the path, or an empty list if no path found.
     */
    public List<Grid> findPath(Grid startGridInput, Grid endGridInput, List<Grid> occupiedCellsInput, boolean endIsObstacle) {
        Grid startGrid = getOrCreateGrid(startGridInput.getX(), startGridInput.getY(), startGridInput.getZ());
        Grid endGrid = getOrCreateGrid(endGridInput.getX(), endGridInput.getY(), endGridInput.getZ());

        System.out.println("PS.findPath (A*): Start: " + startGrid.getX()+","+startGrid.getY()+","+startGrid.getZ() + " (ID:"+startGrid.getId()+")"
                         + ", End: " + endGrid.getX()+","+endGrid.getY()+","+endGrid.getZ() + " (ID:"+endGrid.getId()+"), endIsObstacle: " + endIsObstacle);
        System.out.println("PS.findPath (A*): Received occupiedCellsInput: " +
            occupiedCellsInput.stream().map(g -> "GridID " + g.getId() + " (" + g.getX()+","+g.getY()+","+g.getZ() + ")").collect(Collectors.joining("; ")));

        Node startNode = new Node(startGrid, null, 0, calculateManhattanDistance(startGrid, endGrid));
        Node endNodeTarget = new Node(endGrid, null, 0, 0);

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(node -> node.fCost));
        Set<Grid> closedSet = new HashSet<>();
        Set<Grid> obstacles = occupiedCellsInput.stream()
                                           .map(g -> getOrCreateGrid(g.getX(), g.getY(), g.getZ()))
                                           .collect(Collectors.toSet());
        if (endIsObstacle) {
            obstacles.add(endNodeTarget.grid);
            System.out.println("PS.findPath (A*): EndGrid added to local obstacles because endIsObstacle=true.");
        }
        System.out.println("PS.findPath (A*): Local A* obstacles set: " +
            obstacles.stream().map(g -> "GridID " + g.getId() + " (" + g.getX()+","+g.getY()+","+g.getZ() + ")").collect(Collectors.joining("; ")));

        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            if (currentNode.grid.getX() == endNodeTarget.grid.getX() &&
                currentNode.grid.getY() == endNodeTarget.grid.getY() &&
                currentNode.grid.getZ() == endNodeTarget.grid.getZ()) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.grid);

            for (Grid neighborGridInput : getNeighbors(currentNode.grid)) {
                Grid neighborGrid = getOrCreateGrid(neighborGridInput.getX(), neighborGridInput.getY(), neighborGridInput.getZ()); // Ensure consistent instance
                boolean isObstacle = obstacles.contains(neighborGrid);
                boolean isEndAndAllowed = neighborGrid.equals(endNodeTarget.grid) && !endIsObstacle;

                if (closedSet.contains(neighborGrid)) {
                    System.out.println("PS.findPath (A*): Neighbor " + neighborGrid.getX()+","+neighborGrid.getY()+","+neighborGrid.getZ() + " (ID:"+neighborGrid.getId()+") in closedSet. SKIPPING.");
                    continue;
                }
                if (isObstacle && !isEndAndAllowed) {
                     System.out.println("PS.findPath (A*): Neighbor " + neighborGrid.getX()+","+neighborGrid.getY()+","+neighborGrid.getZ() + " (ID:"+neighborGrid.getId()+") is obstacle and not allowed end. SKIPPING.");
                    continue;
                }

                double tentativeGCost = currentNode.gCost + 1; // Assuming cost of 1 to move to an adjacent cell

                Node neighborNode = new Node(neighborGrid, currentNode, tentativeGCost, calculateManhattanDistance(neighborGrid, endGrid));

                // Check if neighbor is in openList and if this path is better
                boolean inOpenList = false;
                for(Node openNode : openList) {
                    if(openNode.grid.equals(neighborGrid)) {
                        inOpenList = true;
                        if (tentativeGCost < openNode.gCost) {
                            openList.remove(openNode); // Remove and re-add with better path
                            openList.add(neighborNode);
                        }
                        break;
                    }
                }

                if (!inOpenList) {
                    openList.add(neighborNode);
                }
            }
        }
        System.err.println("PathfindingService: A* could not find path from " + startGrid.getX() + "," + startGrid.getY() + "," + startGrid.getZ() +
                           " to " + endGrid.getX() + "," + endGrid.getY() + "," + endGrid.getZ());
        return new ArrayList<>(); // No path found
    }
    
    // Overload for backward compatibility or simple pathing where end is not an obstacle
    public List<Grid> findPath(Grid start, Grid end) {
        return findPath(start, end, Collections.emptyList(), false);
    }


    private List<Grid> reconstructPath(Node endNode) {
        LinkedList<Grid> path = new LinkedList<>();
        Node current = endNode;
        while (current != null) {
            path.addFirst(current.grid);
            current = current.parent;
        }
        return path;
    }

    private List<Grid> getNeighbors(Grid grid) {
        List<Grid> neighbors = new ArrayList<>();
        int[][] DIRS = {
                {0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, // Horizontal
                {0, 0, 1}, {0, 0, -1}                          // Vertical (if applicable)
        };

        for (int[] dir : DIRS) {
            int newX = grid.getX() + dir[0];
            int newY = grid.getY() + dir[1];
            int newZ = grid.getZ() + dir[2];

            if (isValidCoordinate(newX, newY, newZ)) {
                neighbors.add(getOrCreateGrid(newX, newY, newZ));
            }
        }
        return neighbors;
    }
    
    private List<Grid> getAdjacentUnoccupiedCells(Grid center, List<Grid> occupiedCellsGeneral, Grid startCellForDistanceSort) {
        System.out.println("PS.getAdjacentUnoccupiedCells: Center: " + center.getX()+","+center.getY()+","+center.getZ() + " (ID:"+center.getId()+")");
        System.out.println("PS.getAdjacentUnoccupiedCells: Received occupiedCellsGeneral: " +
            occupiedCellsGeneral.stream().map(g -> "GridID " + g.getId() + " (" + g.getX()+","+g.getY()+","+g.getZ() + ")").collect(Collectors.joining("; ")));

        List<Grid> adjacentCells = new ArrayList<>();
        Set<Grid> localObstacles = occupiedCellsGeneral.stream()
                                           .map(g -> getOrCreateGrid(g.getX(), g.getY(), g.getZ()))
                                           .collect(Collectors.toSet());
        Grid centerGridForObstacles = getOrCreateGrid(center.getX(), center.getY(), center.getZ());
        localObstacles.add(centerGridForObstacles);
        System.out.println("PS.getAdjacentUnoccupiedCells: Local obstacles set (incl. center): " +
            localObstacles.stream().map(g -> "GridID " + g.getId() + " (" + g.getX()+","+g.getY()+","+g.getZ() + ")").collect(Collectors.joining("; ")));

        int[][] DIRS = {
            {0, 1, 0}, {0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, // Horizontal preferred
            // {0, 0, 1}, {0, 0, -1} // Vertical, if interaction from above/below is allowed
        };

        for (int[] dir : DIRS) {
            int adjX = center.getX() + dir[0];
            int adjY = center.getY() + dir[1];
            int adjZ = center.getZ() + dir[2];

            if (isValidCoordinate(adjX, adjY, adjZ)) {
                Grid adjacent = getOrCreateGrid(adjX, adjY, adjZ);
                System.out.print("PS.getAdjacentUnoccupiedCells: Checking adjacent " + adjacent.getX()+","+adjacent.getY()+","+adjacent.getZ() + " (ID:"+adjacent.getId()+")");
                if (!localObstacles.contains(adjacent)) {
                    adjacentCells.add(adjacent);
                    System.out.println(" -> ADDED (unoccupied)");
                } else {
                    System.out.println(" -> SKIPPED (in localObstacles)");
                }
            }
        }
        // Sort by distance to the robot's starting cell to prefer closer interaction spots
        if (startCellForDistanceSort != null) {
            adjacentCells.sort(Comparator.comparingDouble(spot -> calculateManhattanDistance(startCellForDistanceSort, spot)));
        }
        return adjacentCells;
    }


    private boolean isValidCoordinate(int x, int y, int z) {
        return x >= 0 && x < MAX_X &&
               y >= 0 && y < MAX_Y &&
               z >= 0 && z < MAX_Z;
    }

    private double calculateManhattanDistance(Grid a, Grid b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private Grid getOrCreateGrid(int x, int y, int z) {
        return gridRepository.findFirstByXAndYAndZ(x, y, z)
                .orElseGet(() -> {
                    // System.out.println("PathfindingService: Creating and saving new grid at: " + x + "," + y + "," + z); // Can be noisy
                    return gridRepository.save(new Grid(x, y, z));
                });
    }
}