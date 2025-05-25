package com.irankiai.backend.Task;

public enum TaskStatus {
    CREATED,
    ASSIGNED,
    IN_PROGRESS,
    COLLECTING,
    DELIVERING,
    COMPLETED,
    FAILED,
    CANCELLED,
    WAITING_FOR_INVENTORY,
    WAITING_FOR_ROBOT,     // New or ensure it exists
    ERROR_PATHFINDING      // New or ensure it exists
}