package com.irankiai.backend.Task;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;
import com.irankiai.backend.DeliverOrder.DeliverOrder;
import com.irankiai.backend.DeliverOrder.DeliverOrderRepository;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.irankiai.backend.Task.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ContainerRepository containerRepository;

    @Autowired
    private RobotRepository robotRepository;

    @Autowired
    private DeliverOrderRepository deliverOrderRepository;

    @Autowired
    private PathfindingService pathfindingService;

    @Autowired
    private GridRepository gridRepository;

    @Transactional
    public Task createTaskForOrder(Order order) {
        // Create a new task
        Task task = new Task();
        task.setOrder(order);
        
        // Add this line - missing variable declaration
        boolean hasAllInventory = true;
        
        // Create deliver order
        DeliverOrder deliverOrder = new DeliverOrder();
        
        // Set deliver location (could be a fixed pickup point)
        Grid deliverLocation = new Grid(0, 0, 0); // Example location
        
        // Save the Grid BEFORE associating it with DeliverOrder
        deliverLocation = gridRepository.save(deliverLocation);
        
        deliverOrder.setLocation(deliverLocation);
        deliverOrderRepository.save(deliverOrder);
        
        task.setDeliverOrder(deliverOrder);
        order.setDeliverOrder(deliverOrder);

        // Find containers for each product in the order
        for (OrderItem orderItem : order.getItems()) {
            Product product = orderItem.getProduct();
            int quantityNeeded = orderItem.getQuantity();

            // Find containers with this product
            List<Container> containers = findContainersWithProduct(product);

            if (containers.isEmpty()) {
                // Instead of throwing exception, mark as missing and cache the task
                task.addMissingProduct(product, quantityNeeded);
                hasAllInventory = false;
                continue; // Skip to next product
            }

            int totalAvailableQuantity = 0;
            // Calculate total available quantity across all containers
            for (Container container : containers) {
                totalAvailableQuantity += getProductQuantityInContainer(container, product);
            }

            if (totalAvailableQuantity < quantityNeeded) {
                // Not enough inventory, track what's missing
                task.addMissingProduct(product, quantityNeeded - totalAvailableQuantity);
                hasAllInventory = false;

                // Only create task items for available inventory
                quantityNeeded = totalAvailableQuantity;
            }

            // Create task items for available inventory
            if (quantityNeeded > 0) {
                int remainingNeeded = quantityNeeded;

                for (Container container : containers) {
                    // Figure out how much to take from this container
                    int containerQuantity = getProductQuantityInContainer(container, product);
                    int quantityToTake = Math.min(remainingNeeded, containerQuantity);

                    if (quantityToTake <= 0) {
                        continue;
                    }

                    TaskItem taskItem = new TaskItem(product, container, quantityToTake);
                    task.addItem(taskItem);

                    remainingNeeded -= quantityToTake;

                    if (remainingNeeded <= 0) {
                        break;
                    }
                }
            }
        }

        if (hasAllInventory) {
            // If we have all inventory, proceed with normal task creation
            Robot robot = findAvailableRobot();
            if (robot == null) {
                throw new RuntimeException("No available robots found");
            }
            task.setAssignedRobot(robot);

            // Create a path for the robot to follow
            createPathForTask(task);

            // Set status and save
            task.setStatus(TaskStatus.ASSIGNED);
        } else {
            // If we're missing inventory, set the status to waiting
            task.setStatus(TaskStatus.WAITING_FOR_INVENTORY);
            // We'll assign a robot and create a path when inventory becomes available
        }

        return taskRepository.save(task);
    }

    private Robot findAvailableRobot() {
        List<Robot> robots = robotRepository.findAll();

        // Find a robot that isn't already assigned to a task
        for (Robot robot : robots) {
            if (!robot.isCarryingContainer()) {
                // Check if robot is not assigned to any active task
                List<Task> activeTasks = taskRepository.findByAssignedRobotAndStatusIn(
                        robot,
                        List.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS, TaskStatus.COLLECTING,
                                TaskStatus.DELIVERING));

                if (activeTasks.isEmpty()) {
                    return robot;
                }
            }
        }

        return null;
    }

    private List<Container> findContainersWithProduct(Product product) {
        // This is a simplified example - you would need to implement
        // logic to find containers that contain the specified product
        return containerRepository.findByProductsId(product.getId());
    }

    private int getProductQuantityInContainer(Container container, Product product) {
        // This is a simplified example - you would need to implement
        // logic to determine how much of a product is in a container
        return 10; // Example quantity
    }

    private void createPathForTask(Task task) {
        Robot robot = task.getAssignedRobot();
        
        // Remove these problematic lines with undefined variables
        // Grid waypoint = new Grid(x, y, z);
        // waypoint = gridRepository.save(waypoint);
        // path.addWaypoint(waypoint);
        
        // Create a list to store waypoints
        List<Grid> waypoints = new ArrayList<>();
        
        // Start at robot's current location
        Grid currentLocation = robot.getLocation();
        
        // Add container locations to waypoints
        for (TaskItem item : task.getItems()) {
            Grid containerLocation = item.getSourceContainer().getLocation();
            
            // Add path from current location to container
            List<Grid> pathToContainer = pathfindingService.findPath(currentLocation, containerLocation);
            waypoints.addAll(pathToContainer);
            
            // Update current location
            currentLocation = containerLocation;
        }
        
        // Add path to deliver location
        Grid deliverLocation = task.getDeliverOrder().getLocation();
        List<Grid> pathToDeliver = pathfindingService.findPath(currentLocation, deliverLocation);
        waypoints.addAll(pathToDeliver);
        
        // Create and assign path
        Path path = new Path(robot, waypoints);
        task.setPath(path);
    }
    public Task getTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
    }

    public List<Task> getActiveTasks() {
        return taskRepository.findByStatusIn(
                List.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS, TaskStatus.COLLECTING, TaskStatus.DELIVERING));
    }

    public Task updateTaskStatus(Long taskId, TaskStatus status) {
        Task task = getTask(taskId);
        task.setStatus(status);
        return taskRepository.save(task);
    }

    @Autowired
    private ProductRepository productRepository;

    @Scheduled(fixedRate = 60000) // Check every minute
    @Transactional
    public void checkAndActivateWaitingTasks() {
        List<Task> waitingTasks = taskRepository.findByStatus(TaskStatus.WAITING_FOR_INVENTORY);

        for (Task task : waitingTasks) {
            boolean canActivate = true;
            Map<Integer, Integer> stillMissing = new HashMap<>();

            // Check each missing product
            for (Map.Entry<Integer, Integer> entry : task.getMissingProducts().entrySet()) {
                Integer productId = entry.getKey();
                Integer quantityNeeded = entry.getValue();

                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
                // Check if we have enough inventory now
                List<Container> containers = findContainersWithProduct(product);

                int totalAvailableQuantity = 0;
                for (Container container : containers) {
                    totalAvailableQuantity += getProductQuantityInContainer(container, product);
                }

                if (totalAvailableQuantity < quantityNeeded) {
                    // Still don't have enough inventory
                    stillMissing.put(productId, quantityNeeded - totalAvailableQuantity);
                    canActivate = false;

                    // Create task items for any newly available inventory
                    int newlyAvailable = totalAvailableQuantity;
                    if (newlyAvailable > 0) {
                        int remainingNeeded = newlyAvailable;

                        for (Container container : containers) {
                            int containerQuantity = getProductQuantityInContainer(container, product);
                            int quantityToTake = Math.min(remainingNeeded, containerQuantity);

                            if (quantityToTake <= 0) {
                                continue;
                            }

                            TaskItem taskItem = new TaskItem(product, container, quantityToTake);
                            task.addItem(taskItem);

                            remainingNeeded -= quantityToTake;

                            if (remainingNeeded <= 0) {
                                break;
                            }
                        }
                    }
                } else {
                    // We have enough inventory now, create task items
                    int remainingNeeded = quantityNeeded;

                    for (Container container : containers) {
                        int containerQuantity = getProductQuantityInContainer(container, product);
                        int quantityToTake = Math.min(remainingNeeded, containerQuantity);

                        if (quantityToTake <= 0) {
                            continue;
                        }

                        TaskItem taskItem = new TaskItem(product, container, quantityToTake);
                        task.addItem(taskItem);

                        remainingNeeded -= quantityToTake;

                        if (remainingNeeded <= 0) {
                            break;
                        }
                    }
                }
            }

            // Update the task
            task.setMissingProducts(stillMissing);

            // If we can activate the task
            if (canActivate) {
                // Find an available robot
                Robot robot = findAvailableRobot();
                if (robot != null) {
                    task.setAssignedRobot(robot);

                    // Create a path for the robot to follow
                    createPathForTask(task);

                    // Set status to assigned
                    task.setStatus(TaskStatus.ASSIGNED);

                    System.out.println("Activating previously waiting task: " + task.getId());
                } else {
                    // No robot available, keep waiting
                    System.out.println("Task " + task.getId() + " has all inventory but no robot available");
                }
            }

            taskRepository.save(task);
        }
    }
}