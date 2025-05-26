package com.irankiai.backend.Robot;

import com.irankiai.backend.Cache.CacheRepository; // Added
import com.irankiai.backend.ChargingStation.ChargingStationRepository; // Added
import com.irankiai.backend.CollectOrder.CollectOrder;
import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;
import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Grid.GridRepository;
import com.irankiai.backend.Path.Path;
import com.irankiai.backend.Path.PathfindingService;
import com.irankiai.backend.Task.Task;
import com.irankiai.backend.Task.TaskItem;
import com.irankiai.backend.Task.TaskRepository;
import com.irankiai.backend.Task.TaskStatus;
import jakarta.persistence.EntityManager;
import java.util.ArrayList; // Added
import java.util.List;
import java.util.Objects; // Added
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors; // Added
// Potentially CollectOrderRepository, DeliverOrderRepository if non-target ones are obstacles

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableScheduling
public class RobotTaskExecutionService {

    private final GridRepository gridRepository;
    private final TaskRepository taskRepository;
    private final ContainerRepository containerRepository;
    private final EntityManager entityManager;
    private final PathfindingService pathfindingService;
    private final RobotRepository robotRepository; // Added
    private final ChargingStationRepository chargingStationRepository; // Added
    private final CacheRepository cacheRepository; // Added
    // private final CollectOrderRepository collectOrderRepository; // If needed for obstacles
    // private final DeliverOrderRepository deliverOrderRepository; // If needed for obstacles

    private final ReentrantLock taskLock = new ReentrantLock();

    @Autowired
    public RobotTaskExecutionService(
        GridRepository gridRepository,
        TaskRepository taskRepository,
        ContainerRepository containerRepository,
        EntityManager entityManager,
        PathfindingService pathfindingService,
        RobotRepository robotRepository, // Added
        ChargingStationRepository chargingStationRepository, // Added
        CacheRepository cacheRepository
        // CollectOrderRepository collectOrderRepository, // If needed
        // DeliverOrderRepository deliverOrderRepository // If needed // Added
    ) {
        this.gridRepository = gridRepository;
        this.taskRepository = taskRepository;
        this.containerRepository = containerRepository;
        this.entityManager = entityManager;
        this.pathfindingService = pathfindingService;
        this.robotRepository = robotRepository; // Added
        this.chargingStationRepository = chargingStationRepository; // Added
        this.cacheRepository = cacheRepository; // Added
        // this.collectOrderRepository = collectOrderRepository;
        // this.deliverOrderRepository = deliverOrderRepository;
    }

    @Scheduled(fixedRate = 2000)
    @Transactional
    public void executeRobotTasks() {
        if (!taskLock.tryLock()) {
            return;
        }
        try {
            List<TaskStatus> statusesToQuery = List.of(
                TaskStatus.ASSIGNED,
                TaskStatus.COLLECTING,
                TaskStatus.DELIVERING,
                TaskStatus.IN_PROGRESS
            );
            List<Task> activeTasks = taskRepository.findAllByStatusIn(
                statusesToQuery
            );

            for (Task task : activeTasks) {
                Robot robotEntity = task.getAssignedRobot();
                if (robotEntity == null) continue;

                if (!entityManager.contains(robotEntity)) robotEntity =
                    entityManager.merge(robotEntity);
                final Robot currentRobot = robotEntity; // Make effectively final for lambda

                // Manage locations to ensure they are attached entities
                if (
                    currentRobot.getLocation() != null &&
                    !entityManager.contains(currentRobot.getLocation())
                ) currentRobot.setLocation(
                    getOrCreateGrid(currentRobot.getLocation())
                ); // Use getOrCreateGrid for consistency
                if (
                    currentRobot.isCarryingContainer() &&
                    currentRobot.getContainer() != null
                ) {
                    Container carried = currentRobot.getContainer();
                    if (!entityManager.contains(carried)) carried =
                        entityManager.merge(carried);
                    currentRobot.setContainer(carried);
                    if (
                        carried.getLocation() != null &&
                        !entityManager.contains(carried.getLocation())
                    ) carried.setLocation(
                        getOrCreateGrid(carried.getLocation())
                    );
                }
                if (
                    task.getPath() != null &&
                    !entityManager.contains(task.getPath())
                ) task.setPath(entityManager.merge(task.getPath()));
                if (task.getCollectOrder() != null) {
                    CollectOrder co = task.getCollectOrder();
                    if (!entityManager.contains(co)) co = entityManager.merge(
                        co
                    );
                    task.setCollectOrder(co);
                    if (
                        co.getLocation() != null &&
                        !entityManager.contains(co.getLocation())
                    ) co.setLocation(getOrCreateGrid(co.getLocation()));
                    if (
                        co.getContainer() != null &&
                        !entityManager.contains(co.getContainer())
                    ) co.setContainer(entityManager.merge(co.getContainer()));
                }
                if (
                    task.getProductDeliveryTargetLocation() != null &&
                    !entityManager.contains(
                        task.getProductDeliveryTargetLocation()
                    )
                ) {
                    task.setProductDeliveryTargetLocation(
                        getOrCreateGrid(task.getProductDeliveryTargetLocation())
                    );
                }
                if (task.getDeliverOrder() != null) {
                    com.irankiai.backend.DeliverOrder.DeliverOrder dOrder =
                        task.getDeliverOrder();
                    if (!entityManager.contains(dOrder)) dOrder =
                        entityManager.merge(dOrder);
                    task.setDeliverOrder(dOrder);
                    if (
                        dOrder.getLocation() != null &&
                        !entityManager.contains(dOrder.getLocation())
                    ) dOrder.setLocation(getOrCreateGrid(dOrder.getLocation()));
                }

                if (task.getStatus() == TaskStatus.ASSIGNED) {
                    // If it's a product delivery task with a collect order
                    if (
                        task.getCollectOrder() != null &&
                        task.getProductDeliveryTargetContainerId() != null &&
                        task
                            .getItems()
                            .stream()
                            .anyMatch(item -> !item.isCollected())
                    ) {
                        task.setStatus(TaskStatus.COLLECTING);
                        // Path to CollectOrder should have been set by TaskService, or set it here if not.
                        if (
                            task.getPath() == null ||
                            task.getPath().getWaypoints().isEmpty()
                        ) {
                            System.out.println(
                                "Task " +
                                task.getId() +
                                ": Path not set for collecting. Setting path to CollectOrder " +
                                task.getCollectOrder().getId()
                            );
                            setPathToTarget(
                                currentRobot,
                                task,
                                task.getCollectOrder().getLocation(),
                                "CollectOrder " + task.getCollectOrder().getId()
                            );
                        }
                        System.out.println(
                            "Task " +
                            task.getId() +
                            " (Product Delivery) status ASSIGNED -> COLLECTING."
                        );
                    }
                    // Add other task type handling here, e.g., for full order fulfillment using task.getDeliverOrder()
                    else if (
                        task.getOrder() != null &&
                        task.getDeliverOrder() != null &&
                        task.getDeliverOrder().getLocation() != null
                    ) {
                        task.setStatus(TaskStatus.IN_PROGRESS); // Or a more specific status for full order collection
                        if (
                            task.getPath() == null ||
                            task.getPath().getWaypoints().isEmpty()
                        ) {
                            System.out.println(
                                "Task " +
                                task.getId() +
                                ": Path not set for full order. Setting path to DeliverOrder " +
                                task.getDeliverOrder().getId()
                            );
                            setPathToTarget(
                                currentRobot,
                                task,
                                task.getDeliverOrder().getLocation(),
                                "Full Order Delivery Location"
                            );
                        }
                        System.out.println(
                            "Task " +
                            task.getId() +
                            " (Full Order) status ASSIGNED -> IN_PROGRESS."
                        );
                    } else {
                        System.err.println(
                            "Task " +
                            task.getId() +
                            " is ASSIGNED but has unclear next step. Setting to FAILED."
                        );
                        task.setStatus(TaskStatus.FAILED);
                    }
                }

                if (task.getStatus() == TaskStatus.COLLECTING) {
                    CollectOrder collectOrder = task.getCollectOrder();
                    if (
                        collectOrder == null ||
                        collectOrder.getLocation() == null
                    ) {
                        task.setStatus(TaskStatus.FAILED);
                        System.err.println(
                            "Task " +
                            task.getId() +
                            " (COLLECTING) failed: CollectOrder or its location is null."
                        );
                    } else {
                        Grid actualCollectLocation = getOrCreateGrid(
                            collectOrder.getLocation()
                        );
                        Path currentPath = task.getPath();
                        boolean atInteractionSpotForCollecting = false;

                        if (
                            currentPath != null &&
                            currentPath.isCompleted() &&
                            !currentPath.getWaypoints().isEmpty()
                        ) {
                            Grid lastWaypoint = getOrCreateGrid(
                                currentPath
                                    .getWaypoints()
                                    .get(currentPath.getWaypoints().size() - 1)
                            );
                            if (
                                isAtPosition(
                                    currentRobot.getLocation(),
                                    lastWaypoint
                                )
                            ) {
                                atInteractionSpotForCollecting = true;
                            }
                        }

                        if (atInteractionSpotForCollecting) {
                            if (
                                areAdjacent(
                                    currentRobot.getLocation(),
                                    actualCollectLocation
                                )
                            ) {
                                System.out.println(
                                    "Robot " +
                                    currentRobot.getId() +
                                    " at interaction spot, adjacent to CollectOrder " +
                                    collectOrder.getId() +
                                    " for task " +
                                    task.getId()
                                );
                                // Simulate collecting items
                                task
                                    .getItems()
                                    .stream()
                                    .filter(item -> !item.isCollected())
                                    .findFirst() // Process one item type or step at a time if complex
                                    .ifPresent(itemToCollect -> {
                                        System.out.println(
                                            "Robot " +
                                            currentRobot.getId() +
                                            " 'collecting' product " +
                                            itemToCollect
                                                .getProduct()
                                                .getName() +
                                            " from CollectOrder's container (simulated). Item marked collected."
                                        );
                                        itemToCollect.setCollected(true);
                                    });

                                if (
                                    task
                                        .getItems()
                                        .stream()
                                        .allMatch(TaskItem::isCollected)
                                ) {
                                    if (
                                        task.getProductDeliveryTargetContainerId() !=
                                            null &&
                                        task.getProductDeliveryTargetLocation() !=
                                        null
                                    ) {
                                        task.setStatus(TaskStatus.DELIVERING);
                                        setPathToTarget(
                                            currentRobot,
                                            task,
                                            task.getProductDeliveryTargetLocation(),
                                            "Target Container " +
                                            task.getProductDeliveryTargetContainerId()
                                        );
                                        System.out.println(
                                            "Task " +
                                            task.getId() +
                                            " (Product Delivery) status COLLECTING -> DELIVERING to Container " +
                                            task.getProductDeliveryTargetContainerId()
                                        );
                                    } else if (
                                        task.getOrder() != null &&
                                        task.getDeliverOrder() != null &&
                                        task.getDeliverOrder().getLocation() !=
                                        null
                                    ) {
                                        task.setStatus(TaskStatus.DELIVERING);
                                        setPathToTarget(
                                            currentRobot,
                                            task,
                                            task
                                                .getDeliverOrder()
                                                .getLocation(),
                                            "Order Final Delivery Location " +
                                            task.getDeliverOrder().getId()
                                        );
                                        System.out.println(
                                            "Task " +
                                            task.getId() +
                                            " (Full Order) status COLLECTING -> DELIVERING to Order Location"
                                        );
                                    } else {
                                        System.err.println(
                                            "Task " +
                                            task.getId() +
                                            " finished collecting, but no clear delivery target. Setting to FAILED."
                                        );
                                        task.setStatus(TaskStatus.FAILED);
                                    }
                                } else {
                                    System.out.println(
                                        "Robot " +
                                        currentRobot.getId() +
                                        " still has items to collect for task " +
                                        task.getId()
                                    );
                                    // If there are more items from the SAME collectOrder, it stays here.
                                    // If next item is from a DIFFERENT source, logic would need to change.
                                }
                            } else {
                                System.err.println(
                                    "Robot " +
                                    currentRobot.getId() +
                                    " at end of path but NOT adjacent to CollectOrder " +
                                    collectOrder.getId() +
                                    ". Task " +
                                    task.getId() +
                                    " to FAILED."
                                );
                                task.setStatus(TaskStatus.FAILED);
                            }
                        } else {
                            moveRobotAlongPath(currentRobot, task);
                        }
                    }
                }

                if (task.getStatus() == TaskStatus.DELIVERING) {
                    Path currentPath = task.getPath();
                    boolean atInteractionSpotForDelivering = false;
                    Grid actualDeliveryTargetLocation = null;
                    String deliveryTargetName = "Unknown";

                    if (
                        task.getProductDeliveryTargetContainerId() != null &&
                        task.getProductDeliveryTargetLocation() != null
                    ) {
                        actualDeliveryTargetLocation = getOrCreateGrid(
                            task.getProductDeliveryTargetLocation()
                        );
                        deliveryTargetName =
                            "Target Container " +
                            task.getProductDeliveryTargetContainerId();
                    } else if (
                        task.getOrder() != null &&
                        task.getDeliverOrder() != null &&
                        task.getDeliverOrder().getLocation() != null
                    ) {
                        actualDeliveryTargetLocation = getOrCreateGrid(
                            task.getDeliverOrder().getLocation()
                        );
                        deliveryTargetName =
                            "Order Final Delivery Location " +
                            task.getDeliverOrder().getId();
                    }

                    if (actualDeliveryTargetLocation == null) {
                        task.setStatus(TaskStatus.FAILED);
                        System.err.println(
                            "Task " +
                            task.getId() +
                            " (DELIVERING) failed: Actual delivery target location is null."
                        );
                    } else {
                        if (
                            currentPath != null &&
                            currentPath.isCompleted() &&
                            !currentPath.getWaypoints().isEmpty()
                        ) {
                            Grid lastWaypoint = getOrCreateGrid(
                                currentPath
                                    .getWaypoints()
                                    .get(currentPath.getWaypoints().size() - 1)
                            );
                            if (
                                isAtPosition(
                                    currentRobot.getLocation(),
                                    lastWaypoint
                                )
                            ) {
                                atInteractionSpotForDelivering = true;
                            }
                        }

                        if (atInteractionSpotForDelivering) {
                            if (
                                areAdjacent(
                                    currentRobot.getLocation(),
                                    actualDeliveryTargetLocation
                                )
                            ) {
                                System.out.println(
                                    "Robot " +
                                    currentRobot.getId() +
                                    " at interaction spot, adjacent to " +
                                    deliveryTargetName +
                                    " for task " +
                                    task.getId()
                                );
                                // Actual delivery logic
                                if (
                                    task.getProductDeliveryTargetContainerId() !=
                                    null
                                ) {
                                    Container targetContainer =
                                        containerRepository
                                            .findById(
                                                task.getProductDeliveryTargetContainerId()
                                            )
                                            .orElse(null);
                                    if (targetContainer != null) {
                                        System.out.println(
                                            "Robot " +
                                            currentRobot.getId() +
                                            " 'delivering' items to target container " +
                                            targetContainer.getId() +
                                            " (simulated)."
                                        );
                                        // TODO: Logic to transfer items from robot's container to targetContainer
                                        // e.g., for each product in robot.getContainer().getProducts() that matches task items:
                                        //      targetContainer.addProduct(product);
                                        //      robot.getContainer().removeProduct(product);
                                        // containerRepository.save(targetContainer);
                                        // containerRepository.save(robot.getContainer());
                                        task.setStatus(TaskStatus.COMPLETED);
                                        System.out.println(
                                            "Task " +
                                            task.getId() +
                                            " (Product Delivery) COMPLETED. Delivered to Container " +
                                            targetContainer.getId()
                                        );
                                    } else {
                                        task.setStatus(TaskStatus.FAILED);
                                        System.err.println(
                                            "Task " +
                                            task.getId() +
                                            " (Product Delivery) failed: Target container " +
                                            task.getProductDeliveryTargetContainerId() +
                                            " not found at delivery."
                                        );
                                    }
                                } else if (
                                    task.getOrder() != null &&
                                    task.getDeliverOrder() != null
                                ) {
                                    System.out.println(
                                        "Robot " +
                                        currentRobot.getId() +
                                        " 'delivering' full order " +
                                        task.getOrder().getId() +
                                        " (simulated)."
                                    );
                                    // TODO: Logic for full order drop-off
                                    task.setStatus(TaskStatus.COMPLETED);
                                    System.out.println(
                                        "Task " +
                                        task.getId() +
                                        " (Full Order) COMPLETED."
                                    );
                                }
                            } else {
                                System.err.println(
                                    "Robot " +
                                    currentRobot.getId() +
                                    " at end of path but NOT adjacent to " +
                                    deliveryTargetName +
                                    ". Task " +
                                    task.getId() +
                                    " to FAILED."
                                );
                                task.setStatus(TaskStatus.FAILED);
                            }
                        } else {
                            moveRobotAlongPath(currentRobot, task);
                        }
                    }
                }
                // Save task changes at the end of processing for this task
                if (entityManager.contains(task)) { // Check if task is still managed
                    taskRepository.save(task);
                } else {
                    System.err.println(
                        "Task " +
                        task.getId() +
                        " became detached. Changes might not be saved."
                    );
                }
            }
        } catch (Exception e) {
            // Log the exception properly
            System.err.println(
                "Unexpected error in executeRobotTasks: " + e.getMessage()
            );
            e.printStackTrace();
        } finally {
            taskLock.unlock();
        }
    }

    private void setPathToTarget(
        Robot robot,
        Task task,
        Grid targetLocationGrid,
        String targetName
    ) {
        if (targetLocationGrid == null) {
            System.err.println(
                "RTEC.setPathToTarget: Cannot set path for task " +
                task.getId() +
                ": target " +
                targetName +
                " location is null."
            );
            task.setStatus(TaskStatus.ERROR_PATHFINDING);
            return;
        }
        Grid robotCurrentGrid = getOrCreateGrid(robot.getLocation());
        Grid targetGrid = getOrCreateGrid(targetLocationGrid); // This is the actual location of the object to interact with

        System.out.println(
            "RTEC.setPathToTarget for Task " +
            task.getId() +
            ", Robot " +
            robot.getId() +
            " from " +
            robotCurrentGrid.getX() +
            "," +
            robotCurrentGrid.getY() +
            "," +
            robotCurrentGrid.getZ() +
            " to interact with " +
            targetName +
            " at " +
            targetGrid.getX() +
            "," +
            targetGrid.getY() +
            "," +
            targetGrid.getZ() +
            " (GridID: " +
            targetGrid.getId() +
            ")"
        );

        List<Grid> occupiedCells = new ArrayList<>();
        System.out.println(
            "RTEC.setPathToTarget: Initializing occupiedCells list."
        );

        // 1. Other robots' locations and their carried containers
        robotRepository
            .findAll()
            .stream()
            .filter(r -> !Objects.equals(r.getId(), robot.getId())) // Exclude the current robot
            .forEach(otherRobot -> {
                if (otherRobot.getLocation() != null) {
                    Grid otherRobotLoc = getOrCreateGrid(
                        otherRobot.getLocation()
                    );
                    occupiedCells.add(otherRobotLoc);
                    System.out.println(
                        "RTEC.setPathToTarget: Added other robot " +
                        otherRobot.getId() +
                        " location " +
                        otherRobotLoc.getX() +
                        "," +
                        otherRobotLoc.getY() +
                        "," +
                        otherRobotLoc.getZ() +
                        " (GridID: " +
                        otherRobotLoc.getId() +
                        ") to occupiedCells."
                    );
                }
                if (
                    otherRobot.isCarryingContainer() &&
                    otherRobot.getContainer() != null &&
                    otherRobot.getContainer().getLocation() != null
                ) {
                    Grid otherRobotContainerLoc = getOrCreateGrid(
                        otherRobot.getContainer().getLocation()
                    );
                    occupiedCells.add(otherRobotContainerLoc);
                    System.out.println(
                        "RTEC.setPathToTarget: Added other robot " +
                        otherRobot.getId() +
                        "'s carried container " +
                        otherRobot.getContainer().getId() +
                        " location " +
                        otherRobotContainerLoc.getX() +
                        "," +
                        otherRobotContainerLoc.getY() +
                        "," +
                        otherRobotContainerLoc.getZ() +
                        " (GridID: " +
                        otherRobotContainerLoc.getId() +
                        ") to occupiedCells."
                    );
                }
            });

        // 2. Stationary containers
        containerRepository
            .findAll()
            .stream()
            .filter(c -> c.getLocation() != null)
            .forEach(c_obstacle -> {
                Grid cObstacleLocation = getOrCreateGrid(
                    c_obstacle.getLocation()
                );
                boolean isRobotsOwnContainer =
                    robot.isCarryingContainer() &&
                    robot.getContainer() != null &&
                    Objects.equals(
                        robot.getContainer().getId(),
                        c_obstacle.getId()
                    );
                boolean isProductDeliveryTarget =
                    task.getProductDeliveryTargetContainerId() != null &&
                    Objects.equals(
                        task.getProductDeliveryTargetContainerId(),
                        c_obstacle.getId()
                    ) &&
                    cObstacleLocation.getX() == targetGrid.getX() &&
                    cObstacleLocation.getY() == targetGrid.getY() &&
                    cObstacleLocation.getZ() == targetGrid.getZ();
                boolean isCollectOrderTarget =
                    task.getCollectOrder() != null &&
                    task.getCollectOrder().getContainer() != null &&
                    Objects.equals(
                        task.getCollectOrder().getContainer().getId(),
                        c_obstacle.getId()
                    ) &&
                    cObstacleLocation.getX() == targetGrid.getX() &&
                    cObstacleLocation.getY() == targetGrid.getY() &&
                    cObstacleLocation.getZ() == targetGrid.getZ();

                if (isRobotsOwnContainer) {
                    System.out.println(
                        "RTEC.setPathToTarget: Stationary Container " +
                        c_obstacle.getId() +
                        " at " +
                        cObstacleLocation.getX() +
                        "," +
                        cObstacleLocation.getY() +
                        "," +
                        cObstacleLocation.getZ() +
                        " (GridID: " +
                        cObstacleLocation.getId() +
                        ") is robot's own. SKIPPING."
                    );
                    return; // effectively continue
                }
                if (isProductDeliveryTarget) {
                    System.out.println(
                        "RTEC.setPathToTarget: Stationary Container " +
                        c_obstacle.getId() +
                        " at " +
                        cObstacleLocation.getX() +
                        "," +
                        cObstacleLocation.getY() +
                        "," +
                        cObstacleLocation.getZ() +
                        " (GridID: " +
                        cObstacleLocation.getId() +
                        ") is ProductDeliveryTarget and at targetGrid. SKIPPING."
                    );
                    return;
                }
                if (isCollectOrderTarget) {
                    System.out.println(
                        "RTEC.setPathToTarget: Stationary Container " +
                        c_obstacle.getId() +
                        " at " +
                        cObstacleLocation.getX() +
                        "," +
                        cObstacleLocation.getY() +
                        "," +
                        cObstacleLocation.getZ() +
                        " (GridID: " +
                        cObstacleLocation.getId() +
                        ") is CollectOrderTarget and at targetGrid. SKIPPING."
                    );
                    return;
                }
                occupiedCells.add(cObstacleLocation);
                System.out.println(
                    "RTEC.setPathToTarget: Added stationary container " +
                    c_obstacle.getId() +
                    " location " +
                    cObstacleLocation.getX() +
                    "," +
                    cObstacleLocation.getY() +
                    "," +
                    cObstacleLocation.getZ() +
                    " (GridID: " +
                    cObstacleLocation.getId() +
                    ") to occupiedCells."
                );
            });

        // 3. Fixed obstacles like Charging Stations
        chargingStationRepository
            .findAll()
            .stream()
            .filter(cs -> cs.getLocation() != null)
            .forEach(cs -> {
                Grid csLoc = getOrCreateGrid(cs.getLocation());
                occupiedCells.add(csLoc);
                System.out.println(
                    "RTEC.setPathToTarget: Added ChargingStation " +
                    cs.getId() +
                    " location " +
                    csLoc.getX() +
                    "," +
                    csLoc.getY() +
                    "," +
                    csLoc.getZ() +
                    " (GridID: " +
                    csLoc.getId() +
                    ") to occupiedCells."
                );
            });

        // 4. Caches
        cacheRepository
            .findAll()
            .stream()
            .filter(cache -> cache.getLocation() != null)
            .filter(cache -> {
                Grid cacheLocation = getOrCreateGrid(cache.getLocation());
                boolean isTarget =
                    cacheLocation.getX() == targetGrid.getX() &&
                    cacheLocation.getY() == targetGrid.getY() &&
                    cacheLocation.getZ() == targetGrid.getZ();
                if (isTarget) {
                    System.out.println(
                        "RTEC.setPathToTarget: Cache " +
                        cache.getId() +
                        " at " +
                        cacheLocation.getX() +
                        "," +
                        cacheLocation.getY() +
                        "," +
                        cacheLocation.getZ() +
                        " (GridID: " +
                        cacheLocation.getId() +
                        ") is at targetGrid. SKIPPING."
                    );
                }
                return !isTarget;
            })
            .forEach(cache -> {
                Grid cacheLoc = getOrCreateGrid(cache.getLocation());
                occupiedCells.add(cacheLoc);
                System.out.println(
                    "RTEC.setPathToTarget: Added Cache " +
                    cache.getId() +
                    " location " +
                    cacheLoc.getX() +
                    "," +
                    cacheLoc.getY() +
                    "," +
                    cacheLoc.getZ() +
                    " (GridID: " +
                    cacheLoc.getId() +
                    ") to occupiedCells."
                );
            });

        List<Grid> distinctOccupiedCells = occupiedCells
            .stream()
            .distinct()
            .collect(Collectors.toList());
        System.out.println(
            "RTEC.setPathToTarget: Final distinctOccupiedCells to be passed to pathfinding: " +
            distinctOccupiedCells
                .stream()
                .map(
                    g ->
                        "GridID " +
                        g.getId() +
                        " (" +
                        g.getX() +
                        "," +
                        g.getY() +
                        "," +
                        g.getZ() +
                        ")"
                )
                .collect(Collectors.joining("; "))
        );

        List<Grid> waypoints = pathfindingService.findPathToInteract(
            robotCurrentGrid,
            targetGrid,
            distinctOccupiedCells
        );

        if (waypoints != null && !waypoints.isEmpty()) {
            Path newPath = new Path(robot, waypoints);
            task.setPath(newPath);
            System.out.println(
                "Path set for task " +
                task.getId() +
                " for robot " +
                robot.getId() +
                " to interact with " +
                targetName +
                " (object at " +
                targetGrid.getX() +
                "," +
                targetGrid.getY() +
                "," +
                targetGrid.getZ() +
                ", path ends at " +
                waypoints.get(waypoints.size() - 1).getX() +
                "," +
                waypoints.get(waypoints.size() - 1).getY() +
                "," +
                waypoints.get(waypoints.size() - 1).getZ() +
                ")"
            );
        } else {
            task.setStatus(TaskStatus.ERROR_PATHFINDING);
            System.err.println(
                "Could not find path for task " +
                task.getId() +
                " to interact with " +
                targetName +
                " at " +
                targetGrid.getX() +
                "," +
                targetGrid.getY() +
                "," +
                targetGrid.getZ() +
                " from " +
                robotCurrentGrid.getX() +
                "," +
                robotCurrentGrid.getY() +
                "," +
                robotCurrentGrid.getZ()
            );
        }
    }

    private void moveRobotAlongPath(Robot robot, Task task) {
        Path path = task.getPath();
        if (
            path == null ||
            path.getWaypoints() == null ||
            path.getWaypoints().isEmpty()
        ) {
            // System.err.println("Robot " + robot.getId() + " has no path or empty path to follow for task " + task.getId());
            // This can be normal if pathfinding failed and status is ERROR_PATHFINDING, or if path is completed.
            if (
                task.getStatus() != TaskStatus.ERROR_PATHFINDING &&
                task.getStatus() != TaskStatus.COMPLETED &&
                task.getStatus() != TaskStatus.FAILED
            ) {
                System.err.println(
                    "Robot " +
                    robot.getId() +
                    " has no path for active task " +
                    task.getId() +
                    " (" +
                    task.getStatus() +
                    "). Setting to ERROR_PATHFINDING."
                );
                task.setStatus(TaskStatus.ERROR_PATHFINDING);
            }
            return;
        }

        Grid robotCurrentGrid = getOrCreateGrid(robot.getLocation());
        List<Grid> waypoints = path.getWaypoints();
        Grid nextWaypoint = null;

        // Determine the current position of the robot on the path or the next step
        int currentPathIndex = path.getCurrentWaypointIndex(); // Use the stored index

        if (currentPathIndex < 0) currentPathIndex = 0; // Should be initialized to 0

        if (
            isAtPosition(
                robotCurrentGrid,
                getOrCreateGrid(waypoints.get(currentPathIndex))
            )
        ) {
            // Robot is at the current waypoint, try to move to the next one
            if (currentPathIndex + 1 < waypoints.size()) {
                nextWaypoint = getOrCreateGrid(
                    waypoints.get(currentPathIndex + 1)
                );
                path.setCurrentWaypointIndex(currentPathIndex + 1); // Advance index
            } else {
                // Robot is at the last waypoint of the path
                System.out.println(
                    "Robot " +
                    robot.getId() +
                    " reached end of path for task " +
                    task.getId() +
                    " at " +
                    robotCurrentGrid.getX() +
                    "," +
                    robotCurrentGrid.getY() +
                    "," +
                    robotCurrentGrid.getZ()
                );
                path.setCompleted(true);
                // The calling logic (handleCollecting, handleDelivering) will check if isAtPosition(robot, targetInteractionSpot)
                return;
            }
        } else if (currentPathIndex < waypoints.size()) {
            // Robot is not at the current waypoint index, means it's en route or something went wrong.
            // Target the current waypoint index.
            nextWaypoint = getOrCreateGrid(waypoints.get(currentPathIndex));
            System.out.println(
                "Robot " +
                robot.getId() +
                " not at current path index " +
                currentPathIndex +
                ". Moving towards it: " +
                nextWaypoint.getX() +
                "," +
                nextWaypoint.getY() +
                "," +
                nextWaypoint.getZ()
            );
        }

        if (
            nextWaypoint != null &&
            !isAtPosition(robotCurrentGrid, nextWaypoint)
        ) {
            // Simple direct move to the next adjacent waypoint. A* should ensure it's a valid single step.
            Grid newRobotPos = getOrCreateGrid(
                nextWaypoint.getX(),
                nextWaypoint.getY(),
                nextWaypoint.getZ()
            );

            // Optional: Add a last-minute check if the target cell became occupied by another robot since path generation.
            // This adds complexity and might be better handled by more sophisticated path reservation or re-pathing.
            // For now, we trust the path generated.

            robot.setLocation(newRobotPos);
            System.out.println(
                "Robot " +
                robot.getId() +
                " moved along path to " +
                newRobotPos.getX() +
                "," +
                newRobotPos.getY() +
                "," +
                newRobotPos.getZ() +
                " (current path index: " +
                path.getCurrentWaypointIndex() +
                ") for task " +
                task.getId()
            );
            if (robot.isCarryingContainer() && robot.getContainer() != null) {
                Container carriedContainer = entityManager.merge(
                    robot.getContainer()
                ); // Ensure managed
                carriedContainer.setLocation(newRobotPos);
                robot.setContainer(carriedContainer);
            }
        } else if (
            nextWaypoint != null && isAtPosition(robotCurrentGrid, nextWaypoint)
        ) {
            // Robot has arrived at the 'nextWaypoint' which was determined to be currentPathIndex + 1 or currentPathIndex
            // The logic at the start of the next call to moveRobotAlongPath will handle advancing further.
            // System.out.println("Robot " + robot.getId() + " already at next waypoint " + nextWaypoint.getX() + "," + nextWaypoint.getY() + "," + nextWaypoint.getZ() + " for task " + task.getId());
        }
    }

    private Grid getOrCreateGrid(Grid gridFromEntity) {
        if (gridFromEntity == null) throw new IllegalArgumentException(
            "Grid from entity cannot be null for getOrCreateGrid"
        );
        // Ensure the grid entity itself is managed if it has an ID
        if (
            gridFromEntity.getId() != null &&
            !entityManager.contains(gridFromEntity)
        ) {
            Grid managedGrid = entityManager.find(
                Grid.class,
                gridFromEntity.getId()
            );
            if (
                managedGrid != null &&
                managedGrid.getX() == gridFromEntity.getX() &&
                managedGrid.getY() == gridFromEntity.getY() &&
                managedGrid.getZ() == gridFromEntity.getZ()
            ) {
                return managedGrid; // Return the managed instance if it's the same
            }
            // If not found by ID, or if coordinates differ, it might be a new grid or a detached one with a stale ID.
            // Fall through to find by coordinates, which is more reliable for identity if ID is not primary key for equality.
        } else if (
            gridFromEntity.getId() != null &&
            entityManager.contains(gridFromEntity)
        ) {
            return gridFromEntity; // Already managed
        }

        // Find by coordinates or create new if ID is null or entity wasn't managed/matched by ID
        return gridRepository
            .findFirstByXAndYAndZ(
                gridFromEntity.getX(),
                gridFromEntity.getY(),
                gridFromEntity.getZ()
            )
            .orElseGet(() -> {
                System.out.println(
                    "RobotTaskExecutionService: Creating and saving new grid at: " +
                    gridFromEntity.getX() +
                    "," +
                    gridFromEntity.getY() +
                    "," +
                    gridFromEntity.getZ()
                );
                return gridRepository.save(
                    new Grid(
                        gridFromEntity.getX(),
                        gridFromEntity.getY(),
                        gridFromEntity.getZ()
                    )
                );
            });
    }

    private Grid getOrCreateGrid(int x, int y, int z) { // Overload for direct coordinate usage
        return gridRepository
            .findFirstByXAndYAndZ(x, y, z)
            .orElseGet(() -> {
                System.out.println(
                    "RobotTaskExecutionService: Creating and saving new grid at: " +
                    x +
                    "," +
                    y +
                    "," +
                    z
                );
                return gridRepository.save(new Grid(x, y, z));
            });
    }

    private boolean isAtPosition(Grid a, Grid b) {
        if (a == null || b == null) return false;
        Grid managedA = getOrCreateGrid(a); // Ensure both are managed or identified by coords
        Grid managedB = getOrCreateGrid(b);
        return (
            managedA.getX() == managedB.getX() &&
            managedA.getY() == managedB.getY() &&
            managedA.getZ() == managedB.getZ()
        );
    }

    private boolean areAdjacent(Grid g1, Grid g2) {
        if (g1 == null || g2 == null) return false;
        Grid grid1 = getOrCreateGrid(g1);
        Grid grid2 = getOrCreateGrid(g2);

        int dx = Math.abs(grid1.getX() - grid2.getX());
        int dy = Math.abs(grid1.getY() - grid2.getY());
        int dz = Math.abs(grid1.getZ() - grid2.getZ());

        // Check for adjacency (Manhattan distance of 1)
        return (
            (dx == 1 && dy == 0 && dz == 0) ||
            (dx == 0 && dy == 1 && dz == 0) ||
            (dx == 0 && dy == 0 && dz == 1)
        );
    }
}
