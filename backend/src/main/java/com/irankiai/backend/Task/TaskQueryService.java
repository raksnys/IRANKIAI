package com.irankiai.backend.Task;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.irankiai.backend.Order.Order;

@Service
public class TaskQueryService {
    
    private final TaskRepository taskRepository;
    
    @Autowired
    public TaskQueryService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    public Optional<Task> findTaskByOrder(Order order) {
        // Use a manual filter as a fallback for the repository method
        return taskRepository.findAll().stream()
            .filter(task -> task.getOrder() != null && 
                    task.getOrder().getId().equals(order.getId()))
            .findFirst();
    }
}