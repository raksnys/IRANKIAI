package com.irankiai.backend.Task;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks") // Base path for task-related endpoints
public class TaskController {

    private final TaskService taskService;

    // @Autowired // This is not strictly necessary if there's only one constructor
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Existing endpoints (if any) ...

    @PostMapping("/request-product-delivery")
    public ResponseEntity<?> requestProductDelivery(@RequestBody ProductDeliveryRequestDTO request) {
        try {
            // Ensure your DTO has getProductId(), getQuantity(), getTargetContainerId()
            // sourceContainerId is no longer passed to this service method
            Task createdTask = taskService.createProductDeliveryTask(
                request.getProductId(),
                request.getQuantity(),
                request.getTargetContainerId() // Removed request.getSourceContainerId()
            );
            // createdTask should not be null if service method throws exceptions on failure
            // if (createdTask == null) { 
            //     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not create task.");
            // }

            // Check for specific statuses if needed
            if (createdTask.getStatus() == TaskStatus.WAITING_FOR_ROBOT) { // WAITING_FOR_INVENTORY might not be set here anymore
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(createdTask);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (RuntimeException e) {
            // Log the exception e.g., using a logger
            System.err.println("Error creating product delivery task: " + e.getMessage());
            // It's often better to return a more structured error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating task: " + e.getMessage());
        }
    }
}
