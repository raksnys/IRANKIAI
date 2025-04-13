package com.irankiai.backend.Robot;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.DeliverOrder.DeliverOrder;
import com.irankiai.backend.DeliverOrder.DeliverOrderRepository;
import com.irankiai.backend.Grid.Grid;
import com.irankiai.backend.Path.Path;
import com.irankiai.backend.Task.Task;
import com.irankiai.backend.Task.TaskItem;
import com.irankiai.backend.Task.TaskRepository;
import com.irankiai.backend.Task.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
@EnableScheduling
public class RobotTaskExecutionService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    @Autowired
    private RobotService robotService;
    
    @Autowired
    private DeliverOrderRepository deliverOrderRepository;
    
    private final ReentrantLock taskLock = new ReentrantLock();
    
    @Scheduled(fixedRate = 1000)
    @Transactional
    public void executeRobotTasks() {
        taskLock.lock();
        try {
            // Get all active tasks
            List<Task> activeTasks = taskRepository.findByStatusIn(
                    List.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS, TaskStatus.COLLECTING, TaskStatus.DELIVERING));
            
            for (Task task : activeTasks) {
                Robot robot = task.getAssignedRobot();
                
                // If task is just assigned, start execution
                if (task.getStatus() == TaskStatus.ASSIGNED) {
                    task.setStatus(TaskStatus.IN_PROGRESS);
                    taskRepository.save(task);
                    continue;
                }
                
                // Follow path
                Path path = task.getPath();
                if (!path.isCompleted()) {
                    // Get next waypoint
                    Grid nextWaypoint = path.getNextWaypoint();
                    
                    // Move robot towards next waypoint
                    moveRobotTowards(robot, nextWaypoint);
                    
                    // Check if robot has reached the waypoint
                    if (isAtPosition(robot.getLocation(), nextWaypoint)) {
                        path.advanceToNextWaypoint();
                        
                        // Check if we've completed the path
                        if (path.isCompleted()) {
                            // Transition to next phase based on current status
                            if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                                task.setStatus(TaskStatus.COLLECTING);
                            } else if (task.getStatus() == TaskStatus.COLLECTING) {
                                task.setStatus(TaskStatus.DELIVERING);
                            } else if (task.getStatus() == TaskStatus.DELIVERING) {
                                task.setStatus(TaskStatus.COMPLETED);
                            }
                            taskRepository.save(task);
                        }
                    }
                    continue;
                }
                
                // Handle collection phase
                if (task.getStatus() == TaskStatus.COLLECTING) {
                    // Find next uncollected item
                    TaskItem nextItem = task.getItems().stream()
                            .filter(item -> !item.isCollected())
                            .findFirst()
                            .orElse(null);
                    
                    if (nextItem != null) {
                        // Pickup container
                        Container container = nextItem.getSourceContainer();
                        robotService.pickupContainer(robot.getId(), container.getId());
                        
                        // Mark item as collected
                        nextItem.setCollected(true);
                        taskRepository.save(task);
                        
                        // Create new path to next item or to delivery
                        // This is a simplification - you'd need more complex logic here
                    } else {
                        // All items collected, move to delivery phase
                        task.setStatus(TaskStatus.DELIVERING);
                        taskRepository.save(task);
                    }
                }
                
                // Handle delivery phase
                if (task.getStatus() == TaskStatus.DELIVERING) {
                    // Drop container to deliver order
                    DeliverOrder deliverOrder = task.getDeliverOrder();
                    
                    if (robot.isCarryingContainer()) {
                        robotService.dropContainerToDeliverOrder(robot.getId(), deliverOrder.getId());
                    }
                    
                    // Mark task as completed
                    task.setStatus(TaskStatus.COMPLETED);
                    taskRepository.save(task);
                }
            }
        } finally {
            taskLock.unlock();
        }
    }
    
    private void moveRobotTowards(Robot robot, Grid target) {
        // This is a simplified movement - in reality you'd use your existing movement service
        Grid currentPos = robot.getLocation();
        Grid newPos = new Grid(currentPos.getX(), currentPos.getY(), currentPos.getZ());
        
        // Move one step in the direction of the target (prioritize X, then Y, then Z)
        if (currentPos.getX() != target.getX()) {
            newPos.setX(currentPos.getX() + (currentPos.getX() < target.getX() ? 1 : -1));
        } else if (currentPos.getY() != target.getY()) {
            newPos.setY(currentPos.getY() + (currentPos.getY() < target.getY() ? 1 : -1));
        } else if (currentPos.getZ() != target.getZ()) {
            newPos.setZ(currentPos.getZ() + (currentPos.getZ() < target.getZ() ? 1 : -1));
        }
        
        // Update robot position
        robot.setLocation(newPos);
        robotRepository.save(robot);
        
        // Update position of any carried container
        if (robot.isCarryingContainer()) {
            Container container = robot.getContainer();
            container.setLocation(newPos);
            // Save container
        }
    }
    
    private boolean isAtPosition(Grid a, Grid b) {
        return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
    }
}