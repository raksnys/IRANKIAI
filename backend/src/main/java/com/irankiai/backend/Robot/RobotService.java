package com.irankiai.backend.Robot;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.irankiai.backend.Cache.Cache;
import com.irankiai.backend.Cache.CacheRepository;
import com.irankiai.backend.ChargingStation.ChargingStation; // Keep if used, not in current snippet
import com.irankiai.backend.ChargingStation.ChargingStationRepository;
import com.irankiai.backend.CollectOrder.CollectOrder;
import com.irankiai.backend.CollectOrder.CollectOrderRepository;
import com.irankiai.backend.Container.Container; // Added import
import com.irankiai.backend.Container.ContainerRepository; // Added import
import com.irankiai.backend.DeliverOrder.DeliverOrder;
import com.irankiai.backend.DeliverOrder.DeliverOrderRepository;
import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Grid.GridRepository;
import jakarta.persistence.EntityManager;
// No need for a second Autowired import

@Service
public class RobotService {

    private final RobotRepository robotRepository;
    private final ContainerRepository containerRepository; // Make sure this is injected
    private final GridRepository gridRepository;
    private final EntityManager entityManager;
    private final CacheRepository cacheRepository; 
    private final CollectOrderRepository collectOrderRepository; 
    private final DeliverOrderRepository deliverOrderRepository; 
    private final ChargingStationRepository chargingStationRepository;

    public RobotService(RobotRepository robotRepository, 
                        ContainerRepository containerRepository, // Add to constructor
                        GridRepository gridRepository, 
                        EntityManager entityManager,
                        CacheRepository cacheRepository,
                        CollectOrderRepository collectOrderRepository,
                        DeliverOrderRepository deliverOrderRepository,
                        ChargingStationRepository chargingStationRepository) {
        this.robotRepository = robotRepository;
        this.containerRepository = containerRepository; // Initialize
        this.gridRepository = gridRepository;
        this.entityManager = entityManager;
        this.cacheRepository = cacheRepository; 
        this.collectOrderRepository = collectOrderRepository; 
        this.deliverOrderRepository = deliverOrderRepository; 
        this.chargingStationRepository = chargingStationRepository; 
    }

    public List<Robot> getAllRobots() {
        return robotRepository.findAll();
    }

    public Optional<Robot> getRobot(Integer id) {
        return robotRepository.findById(id);
    }

    @Transactional // Ensure the method is transactional
    public Robot addRobot(Robot robot) {
        if (robot.getLocation() == null) {
            robot.setLocation(getOrCreateGrid(0, 0, 0)); // Default location
            System.out.println("Robot " + (robot.getId() == 0 ? "new" : robot.getId()) + " location defaulted to (0,0,0).");
        } else {
            robot.setLocation(getOrCreateGrid(robot.getLocation()));
        }

        if (robot.getContainer() == null) {
            System.out.println("Robot " + (robot.getId() == 0 ? "new" : robot.getId()) + " is being added without a personal container. Creating and assigning one.");
            Container personalContainer = new Container();
            personalContainer.setLocation(robot.getLocation()); 
            
            // Explicitly save the new container BEFORE assigning to robot if cascade is not working or for clarity
            // containerRepository.save(personalContainer); // Uncomment this line if CascadeType is not set or not working

            robot.setContainer(personalContainer);
        } else {
            // If a container is provided with the robot, ensure it's managed
            Container providedContainer = robot.getContainer();
            if (providedContainer.getId() != 0 && !entityManager.contains(providedContainer)) {
                // If it has an ID but isn't managed, it might be detached.
                // Depending on your logic, you might want to merge it or fetch it.
                // For a new robot, if a container is provided, it should ideally be a new one too, or an existing one fetched by ID.
                // For simplicity, if it's a new robot with a new container, cascade should handle it.
                // If it's a new robot with an *existing* container ID, the frontend should send just the ID, and backend should fetch it.
                // This block assumes if a container is provided, it's either new (and cascade will work) or already managed.
                // If it's detached with an ID, merging might be needed:
                // providedContainer = entityManager.merge(providedContainer);
            }
            // Ensure the provided container's location matches the robot's location
            if (providedContainer.getLocation() == null || 
                !(providedContainer.getLocation().getX() == robot.getLocation().getX() &&
                  providedContainer.getLocation().getY() == robot.getLocation().getY() &&
                  providedContainer.getLocation().getZ() == robot.getLocation().getZ())) {
                providedContainer.setLocation(robot.getLocation());
            }
            robot.setContainer(providedContainer);
        }
        return robotRepository.save(robot);
    }

    public void deleteRobot(Integer id) {
        robotRepository.deleteById(id);
    }

    public Robot updateRobot(Robot robot) {
        if (robot.getLocation() != null) {
            robot.setLocation(getOrCreateGrid(robot.getLocation()));
        }
        // If robot has a container, ensure its location is also updated/managed
        if (robot.getContainer() != null) {
            Container c = robot.getContainer();
            if (c.getId() != 0 && !entityManager.contains(c)) {
                c = entityManager.merge(c);
            }
            c.setLocation(robot.getLocation()); // Keep container with robot
            robot.setContainer(c);
        }
        return robotRepository.save(robot);
    }

    public Robot pickupContainer(Integer robotId, Integer containerId) {
        Robot robot = robotRepository.findById(robotId)
            .orElseThrow(() -> new RuntimeException("Robot not found: " + robotId));
        Container container = containerRepository.findById(containerId)
            .orElseThrow(() -> new RuntimeException("Container not found: " + containerId));

        if (robot.isCarryingContainer()) {
            throw new IllegalStateException("Robot " + robotId + " is already carrying a container.");
        }
        if (container.getLocation() == null) {
            throw new IllegalStateException("Container " + containerId + " has no location and cannot be picked up.");
        }
        
        robot = entityManager.merge(robot);
        container = entityManager.merge(container);
        Grid containerGrid = getOrCreateGrid(container.getLocation()); 
        container.setLocation(containerGrid); 

        robot.setContainer(container);
        robot.setLocation(containerGrid); 

        return robotRepository.save(robot);
    }

    public Robot dropContainer(Integer robotId) {
        Robot robot = robotRepository.findById(robotId)
            .orElseThrow(() -> new RuntimeException("Robot not found: " + robotId));
        
        robot = entityManager.merge(robot); 

        if (!robot.isCarryingContainer()) {
            throw new IllegalStateException("Robot " + robotId + " is not carrying a container.");
        }
        Container container = robot.getContainer();
        container = entityManager.merge(container); 

        Grid robotCurrentLocation = getOrCreateGrid(robot.getLocation()); 
        
        container.setLocation(robotCurrentLocation); 
        // containerRepository.save(container); // Saved by cascade from robot or if explicitly needed
        
        robot.setContainer(null);
        return robotRepository.save(robot);
    }

    @Transactional
    public Robot pickupContainerFromCollectOrder(Integer robotId, Integer collectOrderId) {
        Robot robot = robotRepository.findById(robotId)
            .orElseThrow(() -> new RuntimeException("Robot not found: " + robotId));
        CollectOrder collectOrder = collectOrderRepository.findById(collectOrderId)
            .orElseThrow(() -> new RuntimeException("CollectOrder not found: " + collectOrderId));

        robot = entityManager.merge(robot);
        collectOrder = entityManager.merge(collectOrder);
        if (collectOrder.getLocation() != null) {
            collectOrder.setLocation(getOrCreateGrid(collectOrder.getLocation()));
        }


        if (robot.isCarryingContainer()) {
            throw new IllegalStateException("Robot " + robotId + " is already carrying a container.");
        }
        if (collectOrder.getContainer() == null) {
            throw new IllegalStateException("CollectOrder " + collectOrderId + " has no container assigned to pick up.");
        }
        
        Container containerToPickup = collectOrder.getContainer();
        containerToPickup = entityManager.merge(containerToPickup);
        if (containerToPickup.getLocation() != null) {
            containerToPickup.setLocation(getOrCreateGrid(containerToPickup.getLocation()));
        }
        
        if (containerToPickup.getLocation() == null || 
            collectOrder.getLocation() == null ||
            !isAtSamePosition(containerToPickup.getLocation(), collectOrder.getLocation())) {
            throw new IllegalStateException("Container to pick up is not at the CollectOrder location.");
        }

        robot.setContainer(containerToPickup);
        // collectOrder.setContainer(null); // If the container is now "owned" by the robot
        // collectOrderRepository.save(collectOrder);
        
        return robotRepository.save(robot);
    }


    @Transactional
    public Robot dropContainerToDeliverOrder(Integer robotId, Integer deliverOrderId) {
        Robot robot = robotRepository.findById(robotId)
            .orElseThrow(() -> new RuntimeException("Robot not found: " + robotId));
        DeliverOrder deliverOrder = deliverOrderRepository.findById(deliverOrderId)
            .orElseThrow(() -> new RuntimeException("DeliverOrder not found: " + deliverOrderId));
        
        robot = entityManager.merge(robot);
        deliverOrder = entityManager.merge(deliverOrder);
        if (deliverOrder.getLocation() != null) {
            deliverOrder.setLocation(getOrCreateGrid(deliverOrder.getLocation()));
        }

        Grid deliverOrderGridLocation = deliverOrder.getLocation();

        if (deliverOrderGridLocation == null) {
            System.err.println("DeliverOrder " + deliverOrderId + " does not have a location defined.");
            throw new IllegalStateException("DeliverOrder location cannot be null.");
        }

        Integer predefinedTargetContainerId = deliverOrder.getTargetContainerIdForProductDelivery();

        if (predefinedTargetContainerId != null) {
            Container actualTargetContainer = containerRepository.findById(predefinedTargetContainerId)
                .orElseThrow(() -> new RuntimeException("Predefined target container " + predefinedTargetContainerId + " not found."));
            
            actualTargetContainer = entityManager.merge(actualTargetContainer);
            if (actualTargetContainer.getLocation() != null) {
                 actualTargetContainer.setLocation(getOrCreateGrid(actualTargetContainer.getLocation()));
            }

            if (actualTargetContainer.getLocation() != null &&
                isAtSamePosition(actualTargetContainer.getLocation(), deliverOrderGridLocation)) {

                deliverOrder.setContainer(actualTargetContainer); // This might be incorrect if it's product delivery
                System.out.println("DeliverOrder " + deliverOrder.getId() + " marked as fulfilled at target container " + actualTargetContainer.getId());

                if (robot.isCarryingContainer()) {
                    System.err.println("Warning: Robot " + robotId + " was carrying container " + robot.getContainer().getId() 
                        + " but the task was product delivery to existing container " + actualTargetContainer.getId() 
                        + ". The robot's carried container was NOT dropped or changed by this specific call.");
                }
            } else {
                System.err.println("Error: Predefined target container " + predefinedTargetContainerId 
                                   + " is not at the DeliverOrder location (" + deliverOrderGridLocation.getX() + "," + deliverOrderGridLocation.getY() + "," + deliverOrderGridLocation.getZ() 
                                   + "). Actual: " + (actualTargetContainer.getLocation() != null ? 
                                        (actualTargetContainer.getLocation().getX() + "," + actualTargetContainer.getLocation().getY() + "," + actualTargetContainer.getLocation().getZ()) : "null location"));
                throw new IllegalStateException("Target container not at DeliverOrder location.");
            }
        } else if (robot.isCarryingContainer()) {
            if (deliverOrder.hasContainer()) {
                System.err.println("DeliverOrder " + deliverOrderId + " already has a container. Cannot drop another.");
                throw new IllegalStateException("DeliverOrder " + deliverOrderId + " already has a container.");
            }
            Container carriedContainer = robot.getContainer();
            carriedContainer = entityManager.merge(carriedContainer); 

            carriedContainer.setLocation(deliverOrderGridLocation); 
            
            deliverOrder.setContainer(carriedContainer);
            robot.setContainer(null); 
        } else {
            System.err.println("Robot " + robotId + " is not carrying a container for DeliverOrder " + deliverOrderId + ", and it's not a product delivery to a predefined target.");
            throw new IllegalStateException("Robot not carrying container for non-product-delivery task.");
        }

        deliverOrderRepository.save(deliverOrder);
        return robotRepository.save(robot);
    }
    
    @Transactional
    public Robot chargeBattery(Integer robotId, int amount) {
        Robot robot = robotRepository.findById(robotId)
            .orElseThrow(() -> new RuntimeException("Robot not found: " + robotId));
        
        robot = entityManager.merge(robot);

        int currentBattery = robot.getBatteryLevel();
        robot.setBatteryLevel(Math.min(100, currentBattery + amount)); 
        return robotRepository.save(robot);
    }

    @Transactional
    public Robot pickupContainerFromCache(Integer robotId, Integer cacheId) {
        Robot robot = robotRepository.findById(robotId)
            .orElseThrow(() -> new RuntimeException("Robot not found: " + robotId));
        Cache cache = cacheRepository.findById(cacheId)
            .orElseThrow(() -> new RuntimeException("Cache not found: " + cacheId));

        robot = entityManager.merge(robot); 
        cache = entityManager.merge(cache); 
        if (cache.getLocation() != null) {
            cache.setLocation(getOrCreateGrid(cache.getLocation()));
        }

        if (robot.isCarryingContainer()) {
            throw new IllegalStateException("Robot " + robotId + " is already carrying a container.");
        }
        if (!cache.hasContainer()) {
            throw new IllegalStateException("Cache " + cacheId + " does not have a container.");
        }
        
        Container containerToPickup = cache.getContainer();
        containerToPickup = entityManager.merge(containerToPickup); 
        if (containerToPickup.getLocation() != null) {
            containerToPickup.setLocation(getOrCreateGrid(containerToPickup.getLocation()));
        }

        if (cache.getLocation() == null || !isAtSamePosition(robot.getLocation(), cache.getLocation())) {
            throw new IllegalStateException("Robot " + robotId + " is not at the location of Cache " + cacheId + " to pick up the container.");
        }
        
        robot.setContainer(containerToPickup);
        cache.setContainer(null); 
        
        cacheRepository.save(cache);
        return robotRepository.save(robot);
    }

    private Grid getOrCreateGrid(Grid gridFromEntity) {
        if (gridFromEntity == null) throw new IllegalArgumentException("Grid from entity cannot be null");
        if (gridFromEntity.getId() != null) {
             Grid managedGrid = entityManager.find(Grid.class, gridFromEntity.getId());
             if (managedGrid != null && managedGrid.getX() == gridFromEntity.getX() &&
                 managedGrid.getY() == gridFromEntity.getY() && managedGrid.getZ() == gridFromEntity.getZ()) {
                 return managedGrid;
             }
        }
        return getOrCreateGrid(gridFromEntity.getX(), gridFromEntity.getY(), gridFromEntity.getZ());
    }

    private Grid getOrCreateGrid(int x, int y, int z) {
        return gridRepository.findFirstByXAndYAndZ(x, y, z)
                .orElseGet(() -> {
                    System.out.println("RobotService: Creating and saving new grid at: " + x + "," + y + "," + z);
                    return gridRepository.save(new Grid(x, y, z));
                });
    }

    private boolean isAtSamePosition(Grid a, Grid b) {
        if (a == null || b == null) return false;
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }
}