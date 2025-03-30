package com.irankiai.backend.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.irankiai.backend.Cache.Cache;
import com.irankiai.backend.Cache.CacheRepository;
import com.irankiai.backend.ChargingStation.ChargingStation;
import com.irankiai.backend.ChargingStation.ChargingStationRepository;
import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;
import com.irankiai.backend.Robot.Robot;
import com.irankiai.backend.Robot.RobotRepository;
import com.irankiai.backend.CollectOrder.CollectOrder;
import com.irankiai.backend.CollectOrder.CollectOrderRepository;
import com.irankiai.backend.DeliverOrder.DeliverOrder;
import com.irankiai.backend.DeliverOrder.DeliverOrderRepository;

@Service
public class GridService {

    private final GridRepository gridRepository;

    @Autowired
    public GridService(GridRepository gridRepository) {
        this.gridRepository = gridRepository;
    }

    public List<Grid> getAllGrids() {
        return gridRepository.findAll();
    }

    public Optional<Grid> getGrid(Integer id) {
        return gridRepository.findById(id);
    }

    public Grid addGrid(Grid grid) {
        return gridRepository.save(grid);
    }

    public void deleteGrid(Integer id) {
        gridRepository.deleteById(id);
    }

    public Grid updateGrid(Grid grid) {
        return gridRepository.save(grid);
    }

    @Autowired
    private ContainerRepository containerRepository;

    public List<GridDTO> getGridByType(String type) {
        List<GridDTO> result = new ArrayList<>();

        if (type == null || type.isEmpty()) {
            addAllGridTypes(result);
        } else {
            switch (type) {
                case "CONTAINER":
                    addContainersToGrid(result);
                    break;
                case "ROBOT":
                    addRobotsToGrid(result);
                    break;
                case "CHARGING_STATION":
                    addChargingStationsToGrid(result);
                    break;
                case "CACHE":
                    addCachesToGrid(result);
                    break;
                case "COLLECT_ORDER":
                    addCollectOrdersToGrid(result);
                    break;
                case "DELIVER_ORDER":
                    addDeliverOrdersToGrid(result);
                    break;
            }
        }

        return result;
    }

    private void addAllGridTypes(List<GridDTO> result) {
        addContainersToGrid(result);
        addRobotsToGrid(result);
        addChargingStationsToGrid(result);
        addCachesToGrid(result);
        addCollectOrdersToGrid(result);
        addDeliverOrdersToGrid(result);
    }

    private void addContainersToGrid(List<GridDTO> result) {
        List<Container> containers = containerRepository.findAll();
        List<Robot> robots = robotRepository.findAll();

        List<Integer> carriedContainerIds = robots.stream()
                .filter(Robot::isCarryingContainer)
                .map(robot -> robot.getContainer().getId())
                .toList();

        for (Container container : containers) {
            if (!carriedContainerIds.contains(container.getId())) {
                Grid grid = container.getLocation();
                result.add(new GridDTO(grid.getX(), grid.getY(), grid.getZ(), "CONTAINER"));
            }
        }
    }

    @Autowired
    private RobotRepository robotRepository;

    private void addRobotsToGrid(List<GridDTO> result) {
        List<Robot> robots = robotRepository.findAll();
        for (Robot robot : robots) {
            Grid grid = robot.getLocation();
            result.add(new GridDTO(grid.getX(), grid.getY(), grid.getZ(), "ROBOT"));
        }
    }

    @Autowired
    private ChargingStationRepository chargingStationRepository;

    private void addChargingStationsToGrid(List<GridDTO> result) {
        List<ChargingStation> stations = chargingStationRepository.findAll();
        for (ChargingStation station : stations) {
            Grid grid = station.getLocation();
            result.add(new GridDTO(grid.getX(), grid.getY(), grid.getZ(), "CHARGING_STATION"));
        }
    }

    @Autowired
    private CacheRepository cacheRepository;

    private void addCachesToGrid(List<GridDTO> result) {
        List<Cache> caches = cacheRepository.findAll();
        for (Cache cache : caches) {
            Grid grid = cache.getLocation();
            result.add(new GridDTO(grid.getX(), grid.getY(), grid.getZ(), "CACHE"));
        }
    }

    @Autowired
    private CollectOrderRepository collectOrderRepository;

    private void addCollectOrdersToGrid(List<GridDTO> result) {
        List<CollectOrder> collectOrders = collectOrderRepository.findAll();
        for (CollectOrder collectOrder : collectOrders) {
            Grid grid = collectOrder.getLocation();
            result.add(new GridDTO(grid.getX(), grid.getY(), grid.getZ(), "COLLECT_ORDER"));
        }
    }

    @Autowired
    private DeliverOrderRepository deliverOrderRepository;

    private void addDeliverOrdersToGrid(List<GridDTO> result) {
        List<DeliverOrder> deliverOrders = deliverOrderRepository.findAll();
        for (DeliverOrder deliverOrder : deliverOrders) {
            Grid grid = deliverOrder.getLocation();
            result.add(new GridDTO(grid.getX(), grid.getY(), grid.getZ(), "DELIVER_ORDER"));
        }
    }

}