package com.irankiai.backend.Robot;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.irankiai.backend.ChargingStation.ChargingStation;
import com.irankiai.backend.ChargingStation.ChargingStationRepository;
import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;
import com.irankiai.backend.Grid.Grid;

@Service
@EnableScheduling
public class RobotMovementService {

    private final RobotRepository robotRepository;
    private final ContainerRepository containerRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final Random random = new Random();
    private final ReentrantLock movementLock = new ReentrantLock();

    private static final int MAX_X = 48;
    private static final int MAX_Y = 15;
    private static final int MAX_Z = 10;

    @Autowired
    public RobotMovementService(
            RobotRepository robotRepository,
            ContainerRepository containerRepository,
            ChargingStationRepository chargingStationRepository) {
        this.robotRepository = robotRepository;
        this.containerRepository = containerRepository;
        this.chargingStationRepository = chargingStationRepository;
    }

    //@Scheduled(fixedRate = 1000)
    public void moveRobots() {
        movementLock.lock();
        try {
            List<Robot> robots = robotRepository.findAll();
            List<Container> containers = containerRepository.findAll();
            List<ChargingStation> chargingStations = chargingStationRepository.findAll();

            List<Container> standingContainers = containers.stream()
                    .filter(container -> !isContainerCarried(container, robots))
                    .toList();

            for (Robot robot : robots) {
                moveRobotRandomly(robot, robots, standingContainers, chargingStations);
            }
        } finally {
            movementLock.unlock();
        }
    }

    private boolean isContainerCarried(Container container, List<Robot> robots) {
        if (container == null)
            return false;

        return robots.stream()
                .filter(robot -> robot.isCarryingContainer())
                .filter(robot -> robot.getContainer() != null)
                .anyMatch(robot -> robot.getContainer().getId() == container.getId());
    }

    private void moveRobotRandomly(
            Robot robot,
            List<Robot> allRobots,
            List<Container> containers,
            List<ChargingStation> chargingStations) {

        Grid currentPosition = robot.getLocation();
        int currentX = currentPosition.getX();
        int currentY = currentPosition.getY();
        int currentZ = currentPosition.getZ();

        for (int attempt = 0; attempt < 6; attempt++) {
            Grid newPosition = new Grid(currentX, currentY, currentZ);

            int direction = random.nextInt(6);

            switch (direction) {
                case 0:
                    newPosition.setX(currentX + 1);
                    break;
                case 1:
                    newPosition.setX(currentX - 1);
                    break;
                case 2:
                    newPosition.setY(currentY + 1);
                    break;
                case 3:
                    newPosition.setY(currentY - 1);
                    break;
                case 4:
                    newPosition.setZ(currentZ + 1);
                    break;
                case 5:
                    newPosition.setZ(currentZ - 1);
                    break;
            }

            if (newPosition.getX() < 0 || newPosition.getY() < 0 || newPosition.getZ() < 0 ||
                    newPosition.getX() >= MAX_X || newPosition.getY() >= MAX_Y || newPosition.getZ() >= MAX_Z) {
                continue;
            }

            if (isPositionOccupiedByRobot(newPosition, robot, allRobots)) {
                continue;
            }

            if (isPositionOccupiedByContainer(newPosition, containers)) {
                continue;
            }

            if (isPositionOccupiedByChargingStation(newPosition, chargingStations)) {
                continue;
            }

            robot.setLocation(newPosition);

            if (robot.isCarryingContainer()) {
                Container container = robot.getContainer();
                container.setLocation(newPosition);
                containerRepository.save(container);
            }

            robotRepository.save(robot);
            break;
        }
    }

    private boolean isPositionOccupiedByRobot(Grid position, Robot currentRobot, List<Robot> allRobots) {
        return allRobots.stream()
                .filter(robot -> robot.getId() != currentRobot.getId())
                .filter(robot -> robot.getLocation() != null)
                .anyMatch(robot -> isSameXYPosition(robot.getLocation(), position));
    }
    

    private boolean isPositionOccupiedByContainer(Grid position, List<Container> containers) {
        return containers.stream()
                .filter(container -> container.getLocation() != null)
                .anyMatch(container -> isSameXYPosition(container.getLocation(), position));
    }

    private boolean isPositionOccupiedByChargingStation(Grid position, List<ChargingStation> stations) {
        return stations.stream()
                .filter(station -> station.getLocation() != null)
                .anyMatch(station -> isSameXYPosition(station.getLocation(), position));
    }

    private boolean isSameXYPosition(Grid a, Grid b) {
        if (a == null || b == null)
            return false;
        return a.getX() == b.getX() && a.getY() == b.getY();
    }

    private boolean isAtSamePosition(Grid a, Grid b) {
        if (a == null || b == null)
            return false;
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }
}