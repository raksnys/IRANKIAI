package com.irankiai.backend.Robot;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;

import com.irankiai.backend.Cache.Cache;
import com.irankiai.backend.Cache.CacheRepository;

@Service
public class RobotService {

    private final RobotRepository robotRepository;
    private final ContainerRepository containerRepository;

    @Autowired
    private CacheRepository cacheRepository;

    @Autowired
    public RobotService(RobotRepository robotRepository, ContainerRepository containerRepository) {
        this.robotRepository = robotRepository;
        this.containerRepository = containerRepository;
    }

    public List<Robot> getAllRobots() {
        return robotRepository.findAll();
    }

    public Optional<Robot> getRobot(Integer id) {
        return robotRepository.findById(id);
    }

    public Robot addRobot(Robot robot) {
        return robotRepository.save(robot);
    }

    public void deleteRobot(Integer id) {
        robotRepository.deleteById(id);
    }

    public Robot updateRobot(Robot robot) {
        return robotRepository.save(robot);
    }

    public Robot pickupContainer(Integer robotId, Integer containerId) {
        Optional<Robot> robotOpt = robotRepository.findById(robotId);
        Optional<Container> containerOpt = containerRepository.findById(containerId);

        if (robotOpt.isPresent() && containerOpt.isPresent()) {
            Robot robot = robotOpt.get();
            Container container = containerOpt.get();

            if (robot.isCarryingContainer()) {
                return null;
            }

            container.setLocation(robot.getLocation());
            containerRepository.save(container);

            robot.setContainer(container);
            return robotRepository.save(robot);
        }
        return null;
    }

    public Robot dropContainer(Integer robotId) {
        Optional<Robot> robotOpt = robotRepository.findById(robotId);

        if (robotOpt.isPresent()) {
            Robot robot = robotOpt.get();

            if (!robot.isCarryingContainer()) {
                return null;
            }

            Container container = robot.getContainer();
            container.setLocation(robot.getLocation());
            containerRepository.save(container);

            robot.setContainer(null);
            return robotRepository.save(robot);
        }
        return null;
    }

    public Robot chargeBattery(Integer robotId, int chargeAmount) {
        Optional<Robot> robotOpt = robotRepository.findById(robotId);

        if (robotOpt.isPresent()) {
            Robot robot = robotOpt.get();
            int newLevel = Math.min(100, robot.getBatteryLevel() + chargeAmount);
            robot.setBatteryLevel(newLevel);
            return robotRepository.save(robot);
        }
        return null;
    }

    public Robot pickupContainerFromCache(Integer robotId, Integer cacheId) {
        Optional<Robot> robotOpt = robotRepository.findById(robotId);
        Optional<Cache> cacheOpt = cacheRepository.findById(cacheId);

        if (robotOpt.isPresent() && cacheOpt.isPresent()) {
            Robot robot = robotOpt.get();
            Cache cache = cacheOpt.get();

            if (robot.isCarryingContainer()) {
                return null; // Robot is already carrying a container
            }

            if (!cache.hasContainer()) {
                return null; // Cache does not have a container
            }

            Container container = cache.getContainer();

            container.setLocation(robot.getLocation());

            robot.setContainer(container);

            cache.setContainer(null);

            containerRepository.save(container);
            cacheRepository.save(cache);
            return robotRepository.save(robot);
        }
        return null;
    }
}