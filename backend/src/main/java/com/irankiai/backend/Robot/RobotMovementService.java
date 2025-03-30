package com.irankiai.backend.Robot;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;
import com.irankiai.backend.Grid.Grid;

@Service
@EnableScheduling
public class RobotMovementService {

    private final RobotRepository robotRepository;
    private final ContainerRepository containerRepository;
    private final Random random = new Random();

    @Autowired
    public RobotMovementService(RobotRepository robotRepository, ContainerRepository containerRepository) {
        this.robotRepository = robotRepository;
        this.containerRepository = containerRepository;
    }

    // Run every second (1000ms)
    @Scheduled(fixedRate = 1000)
    public void moveRobots() {
        List<Robot> robots = robotRepository.findAll();
        List<Container> containers = containerRepository.findAll();
        
        // Filter out containers that are being carried by robots
        List<Container> standingContainers = containers.stream()
                .filter(container -> !isContainerCarried(container, robots))
                .toList();

        for (Robot robot : robots) {
            moveRobotRandomly(robot, robots, standingContainers);
        }
    }

    private boolean isContainerCarried(Container container, List<Robot> robots) {
        return robots.stream()
                .anyMatch(robot -> robot.isCarryingContainer() && 
                         robot.getContainer().getId() == container.getId());
    }

    // HACK: sudas o ne metodas cia, reikia lock'us pridet.
    private void moveRobotRandomly(Robot robot, List<Robot> allRobots, List<Container> containers) {
        Grid currentPosition = robot.getLocation();
        int currentX = currentPosition.getX();
        int currentY = currentPosition.getY();
        int currentZ = currentPosition.getZ();

        for (int attempt = 0; attempt < 6; attempt++) {
            Grid newPosition = new Grid(currentX, currentY, currentZ);
            
            int direction = random.nextInt(6);
            
            switch (direction) {
                case 0: newPosition.setX(currentX + 1); break;
                case 1: newPosition.setX(currentX - 1); break;
                case 2: newPosition.setY(currentY + 1); break;
                case 3: newPosition.setY(currentY - 1); break;
                case 4: newPosition.setZ(currentZ + 1); break;
                case 5: newPosition.setZ(currentZ - 1); break;
            }
            
            // FIXME: Hardcodinti grid boundaries
            if (newPosition.getX() < 0 || newPosition.getY() < 0 || newPosition.getZ() < 0 ||
                newPosition.getX() >= 44 || newPosition.getY() >= 15 || newPosition.getZ() >= 10) {
                continue; // Try another direction
            }
            
            // FIXME: nu cia tikrinu tik ar robotai ar konteineriai susikerta, nes bbz kaip charging station reiks daryt
            if (isPositionOccupiedByRobot(newPosition, robot, allRobots)) {
                continue;
            }
            
            if (isPositionOccupiedByContainer(newPosition, containers)) {
                continue; 
            }
            
            robot.setLocation(newPosition);
            robotRepository.save(robot);
            
            if (robot.isCarryingContainer()) {
                Container container = robot.getContainer();
                container.setLocation(newPosition);
            }
            
            break;
        }
    }

    private boolean isPositionOccupiedByRobot(Grid position, Robot currentRobot, List<Robot> allRobots) {
        return allRobots.stream()
                .filter(robot -> robot.getId() != currentRobot.getId())
                .anyMatch(robot -> isAtSamePosition(robot.getLocation(), position));
    }

    private boolean isPositionOccupiedByContainer(Grid position, List<Container> containers) {
        return containers.stream()
                .anyMatch(container -> isAtSamePosition(container.getLocation(), position));
    }

    private boolean isAtSamePosition(Grid a, Grid b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }
}