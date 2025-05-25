package com.irankiai.backend.Robot;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

// import org.springframework.beans.factory.annotation.Autowired; // Can be removed if using single constructor
import org.springframework.scheduling.annotation.EnableScheduling;
// import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.irankiai.backend.ChargingStation.ChargingStation;
import com.irankiai.backend.ChargingStation.ChargingStationRepository;
import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;
import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Grid.GridRepository;

@Service
@EnableScheduling
public class RobotMovementService {

    private final RobotRepository robotRepository;
    private final ContainerRepository containerRepository;
    private final ChargingStationRepository chargingStationRepository;
    private final GridRepository gridRepository;
    private final Random random = new Random();
    private final ReentrantLock movementLock = new ReentrantLock();

    private static final int MAX_X = 48;
    private static final int MAX_Y = 15;
    private static final int MAX_Z = 10;

    // @Autowired // This can often be omitted in Spring Boot with a single constructor
    public RobotMovementService(
            RobotRepository robotRepository,
            ContainerRepository containerRepository,
            ChargingStationRepository chargingStationRepository,
            GridRepository gridRepository) {
        this.robotRepository = robotRepository;
        this.containerRepository = containerRepository;
        this.chargingStationRepository = chargingStationRepository;
        this.gridRepository = gridRepository;
    }

    //@Scheduled(fixedRate = 1000)
    @Transactional
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
        if (container == null) { // Primitive int ID cannot be null, so only check container object
            return false;
        }
        // Assuming getId() returns primitive int for Container
        int containerId = container.getId();

        return robots.stream()
                .filter(Robot::isCarryingContainer)
                .map(Robot::getContainer)
                .filter(Objects::nonNull) // Ensure robot's container is not null
                .mapToInt(Container::getId) // Get the ID of the robot's container as int
                .anyMatch(carriedContainerId -> carriedContainerId == containerId); // Correct comparison for int
    }

    private void moveRobotRandomly(
            Robot robot,
            List<Robot> allRobots,
            List<Container> standingContainers,
            List<ChargingStation> chargingStations) {

        Grid currentPosition = robot.getLocation();
        if (currentPosition == null) {
            System.err.println("Robot " + robot.getId() + " has no current location. Cannot move.");
            return;
        }

        int currentX = currentPosition.getX();
        int currentY = currentPosition.getY();
        int currentZ = currentPosition.getZ();

        for (int attempt = 0; attempt < 10; attempt++) {
            Grid newPositionCoordinates = new Grid(currentX, currentY, currentZ);
            int direction = random.nextInt(6);

            switch (direction) {
                case 0: newPositionCoordinates.setX(currentX + 1); break;
                case 1: newPositionCoordinates.setX(currentX - 1); break;
                case 2: newPositionCoordinates.setY(currentY + 1); break;
                case 3: newPositionCoordinates.setY(currentY - 1); break;
                case 4: newPositionCoordinates.setZ(currentZ + 1); break;
                case 5: newPositionCoordinates.setZ(currentZ - 1); break;
            }

            if (newPositionCoordinates.getX() < 0 || newPositionCoordinates.getY() < 0 || newPositionCoordinates.getZ() < 0 ||
                    newPositionCoordinates.getX() >= MAX_X || newPositionCoordinates.getY() >= MAX_Y || newPositionCoordinates.getZ() >= MAX_Z) {
                continue;
            }

            if (isPositionOccupiedByRobot(newPositionCoordinates, robot, allRobots)) {
                continue;
            }

            if (isPositionOccupiedByContainer(newPositionCoordinates, standingContainers)) {
                continue;
            }

            if (isPositionOccupiedByChargingStation(newPositionCoordinates, chargingStations)) {
                continue;
            }

            Grid persistedNewPosition = gridRepository.findFirstByXAndYAndZ(newPositionCoordinates.getX(), newPositionCoordinates.getY(), newPositionCoordinates.getZ())
                    .orElseGet(() -> gridRepository.save(new Grid(newPositionCoordinates.getX(), newPositionCoordinates.getY(), newPositionCoordinates.getZ())));

            robot.setLocation(persistedNewPosition);

            if (robot.isCarryingContainer()) {
                Container carriedContainer = robot.getContainer();
                if (carriedContainer != null) {
                    carriedContainer.setLocation(persistedNewPosition);
                }
            }
            robotRepository.save(robot);
            break;
        }
    }

    private boolean isPositionOccupiedByRobot(Grid positionToCheck, Robot currentRobot, List<Robot> allRobots) {
        if (currentRobot == null) { // Primitive int ID cannot be null, so only check robot object
             return false;
        }
        // Assuming getId() returns primitive int for Robot
        int currentRobotId = currentRobot.getId();

        return allRobots.stream()
                .filter(otherRobot -> otherRobot != null && otherRobot.getId() != currentRobotId) // Exclude self, ensure otherRobot is not null
                .map(Robot::getLocation)
                .filter(Objects::nonNull)
                .anyMatch(robotLocation -> isAtSamePosition(robotLocation, positionToCheck));
    }

    private boolean isPositionOccupiedByContainer(Grid positionToCheck, List<Container> containers) {
        return containers.stream()
                .map(Container::getLocation)
                .filter(Objects::nonNull)
                .anyMatch(containerLocation -> isAtSamePosition(containerLocation, positionToCheck));
    }

    private boolean isPositionOccupiedByChargingStation(Grid positionToCheck, List<ChargingStation> stations) {
        return stations.stream()
                .map(ChargingStation::getLocation)
                .filter(Objects::nonNull)
                .anyMatch(stationLocation -> isAtSamePosition(stationLocation, positionToCheck));
    }

    private boolean isAtSamePosition(Grid a, Grid b) {
        if (a == null || b == null) {
            return false;
        }
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }
}