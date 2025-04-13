package com.irankiai.backend.Task;

public enum TaskStatus {
    CREATED,
    WAITING_FOR_INVENTORY,
    ASSIGNED, 
    IN_PROGRESS, 
    COLLECTING, 
    DELIVERING, 
    COMPLETED, 
    FAILED
}