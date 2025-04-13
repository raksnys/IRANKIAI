package com.irankiai.backend.Task;

import com.irankiai.backend.Order.Order;
import com.irankiai.backend.Robot.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedRobot(Robot robot);
    List<Task> findByStatusIn(List<TaskStatus> statuses);
    List<Task> findByAssignedRobotAndStatusIn(Robot robot, List<TaskStatus> statuses);
    List<Task> findByStatus(TaskStatus status);
    Optional<Task> findByOrder(Order order);
    
}