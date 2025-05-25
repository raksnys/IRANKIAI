package com.irankiai.backend.Task;

import com.irankiai.backend.CollectOrder.CollectOrder;
import com.irankiai.backend.CollectOrder.CollectOrderRepository;
import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;
import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Grid.GridRepository;
import com.irankiai.backend.Order.Order;
import com.irankiai.backend.Order.OrderItem;
import com.irankiai.backend.Path.Path;
import com.irankiai.backend.Path.PathfindingService;
import com.irankiai.backend.Product.Product;
import com.irankiai.backend.Product.ProductRepository;
import com.irankiai.backend.Robot.Robot;
import com.irankiai.backend.Robot.RobotRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map; // For quantity check
import java.util.stream.Collectors; // For quantity check

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ContainerRepository containerRepository;
    @Autowired
    private RobotRepository robotRepository;
    @Autowired
    private PathfindingService pathfindingService;
    @Autowired
    private GridRepository gridRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CollectOrderRepository collectOrderRepository;

    @Transactional
    public Task createProductDeliveryTask(Integer productId, Integer quantityToCollect, Integer targetContainerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        Container finalTargetContainer = containerRepository.findById(targetContainerId)
                .orElseThrow(() -> new RuntimeException("Target container not found: " + targetContainerId));

        if (finalTargetContainer.getLocation() == null) {
            throw new IllegalStateException("Target container " + targetContainerId + " has no location.");
        }
        Grid targetGridLocation = getOrCreateGrid(finalTargetContainer.getLocation());

        CollectOrder sourceCollectOrder = findSourceCollectOrderForProduct(productId, quantityToCollect);
        if (sourceCollectOrder == null || sourceCollectOrder.getLocation() == null) {
            throw new RuntimeException("Suitable CollectOrder for product " + productId + " with quantity " + quantityToCollect + " not found or has no location.");
        }
        if (!entityManager.contains(sourceCollectOrder)) sourceCollectOrder = entityManager.merge(sourceCollectOrder);
        if (sourceCollectOrder.getLocation() != null && !entityManager.contains(sourceCollectOrder.getLocation())) {
            sourceCollectOrder.setLocation(entityManager.merge(sourceCollectOrder.getLocation()));
        }

        Task task = new Task();
        task.setStatus(TaskStatus.CREATED);
        
        task.setProductDeliveryTargetContainerId(finalTargetContainer.getId());
        task.setProductDeliveryTargetLocation(targetGridLocation);
        
        task.setCollectOrder(sourceCollectOrder);

        TaskItem taskItem = new TaskItem();
        taskItem.setProduct(product);
        taskItem.setQuantity(quantityToCollect);
        task.addItem(taskItem);
        
        Robot availableRobot = findAvailableRobot();
        if (availableRobot == null) {
            task.setStatus(TaskStatus.WAITING_FOR_ROBOT);
            throw new RuntimeException("No available robots for the task.");
        }
        if (availableRobot.getContainer() == null) {
             throw new IllegalStateException("Assigned robot " + availableRobot.getId() + " does not have a personal container for transport.");
        }
        task.setAssignedRobot(availableRobot);
        
        Grid robotCurrentGrid = getOrCreateGrid(availableRobot.getLocation());
        Grid collectOrderGrid = getOrCreateGrid(sourceCollectOrder.getLocation());

        List<Grid> waypointsToCollect = pathfindingService.findPath(robotCurrentGrid, collectOrderGrid);
        if (waypointsToCollect == null || waypointsToCollect.isEmpty()) {
             task.setStatus(TaskStatus.ERROR_PATHFINDING);
             throw new RuntimeException("Could not find path for robot to CollectOrder.");
        }
        Path initialPath = new Path(availableRobot, waypointsToCollect);
        task.setPath(initialPath);

        task.setStatus(TaskStatus.ASSIGNED);
        return taskRepository.save(task);
    }

    private CollectOrder findSourceCollectOrderForProduct(Integer productId, Integer quantityNeeded) {
        List<CollectOrder> allCollectOrders = collectOrderRepository.findAll();
        for (CollectOrder co : allCollectOrders) {
            if (!entityManager.contains(co)) co = entityManager.merge(co);
            if (co.getContainer() != null) { 
                Container containerInCollectOrder = co.getContainer();
                if (!entityManager.contains(containerInCollectOrder)) {
                    containerInCollectOrder = entityManager.merge(containerInCollectOrder);
                }
                
                // Corrected product ID comparison and added quantity check
                // Assuming Product.getId() returns Integer. If it returns int, use ==.
                Map<Integer, Long> productCounts = containerInCollectOrder.getProducts().stream()
                    .collect(Collectors.groupingBy(Product::getId, Collectors.counting()));

                if (productCounts.getOrDefault(productId, 0L) >= quantityNeeded) {
                    return co;
                }
            }
        }
        return null; 
    }

    private Robot findAvailableRobot() {
        List<Robot> robots = robotRepository.findAll();
        for (Robot robot : robots) {
            if (!entityManager.contains(robot)) robot = entityManager.merge(robot);
            
            // Corrected list of active statuses. Add MOVING_TO_CHARGE and CHARGING to TaskStatus enum if they exist.
            List<TaskStatus> activeStatuses = List.of(
                TaskStatus.ASSIGNED, 
                TaskStatus.COLLECTING, 
                TaskStatus.DELIVERING, 
                TaskStatus.IN_PROGRESS
                // Add TaskStatus.MOVING_TO_CHARGE, TaskStatus.CHARGING here if they are defined in your enum
            );
            boolean isAssignedToActiveTask = taskRepository.existsByAssignedRobotAndStatusIn(robot, activeStatuses);

            if (!isAssignedToActiveTask && robot.getBatteryLevel() > 10) { 
                 if (robot.getLocation() != null && !entityManager.contains(robot.getLocation())) {
                    robot.setLocation(entityManager.merge(robot.getLocation()));
                 }
                return robot;
            }
        }
        return null;
    }

    private Grid getOrCreateGrid(Grid gridFromEntity) {
        if (gridFromEntity == null) throw new IllegalArgumentException("Grid from entity cannot be null for getOrCreateGrid");
        if (gridFromEntity.getId() != null) {
            if (!entityManager.contains(gridFromEntity)) { 
                Grid foundGrid = gridRepository.findById(gridFromEntity.getId()).orElse(null);
                if (foundGrid != null && foundGrid.getX() == gridFromEntity.getX() &&
                    foundGrid.getY() == gridFromEntity.getY() && foundGrid.getZ() == gridFromEntity.getZ()) {
                    return foundGrid; 
                }
            } else {
                return gridFromEntity; 
            }
        }
        return gridRepository.findFirstByXAndYAndZ(gridFromEntity.getX(), gridFromEntity.getY(), gridFromEntity.getZ())
                .orElseGet(() -> {
                    System.out.println("TaskService: Creating and saving new grid at: " + gridFromEntity.getX() + "," + gridFromEntity.getY() + "," + gridFromEntity.getZ());
                    return gridRepository.save(new Grid(gridFromEntity.getX(), gridFromEntity.getY(), gridFromEntity.getZ()));
                });
    }
    
    @Transactional
    public Task createTaskForOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        com.irankiai.backend.DeliverOrder.DeliverOrder finalDeliverOrder = order.getDeliverOrder();
        if (finalDeliverOrder == null || finalDeliverOrder.getLocation() == null) {
            throw new IllegalStateException("Order must have a DeliverOrder with a location for the final destination.");
        }
        finalDeliverOrder.setLocation(getOrCreateGrid(finalDeliverOrder.getLocation()));

        Task task = new Task();
        task.setOrder(order); 
        task.setDeliverOrder(finalDeliverOrder); 

        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Order has no items to create a task for.");
        }

        for (OrderItem orderItem : order.getItems()) {
            TaskItem taskItem = new TaskItem();
            taskItem.setProduct(orderItem.getProduct());
            taskItem.setQuantity(orderItem.getQuantity());
            task.addItem(taskItem);
        }
        
        Robot availableRobot = findAvailableRobot();
        if (availableRobot == null) {
            task.setStatus(TaskStatus.WAITING_FOR_ROBOT);
        } else {
            task.setAssignedRobot(availableRobot);
            task.setStatus(TaskStatus.WAITING_FOR_INVENTORY); 
        }
        return taskRepository.save(task);
    }
}