package com.irankiai.backend.Task;

public class ProductDeliveryRequestDTO {
    private Integer productId;
    private Integer quantity; // Added
    private Integer targetContainerId; // Renamed from containerId for clarity, or ensure it means target
    private Integer sourceContainerId; // Added

    // Constructors, Getters, and Setters

    public ProductDeliveryRequestDTO() {
    }

    public ProductDeliveryRequestDTO(Integer productId, Integer quantity, Integer targetContainerId, Integer sourceContainerId) {
        this.productId = productId;
        this.quantity = quantity;
        this.targetContainerId = targetContainerId;
        this.sourceContainerId = sourceContainerId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getTargetContainerId() {
        return targetContainerId;
    }

    public void setTargetContainerId(Integer targetContainerId) {
        this.targetContainerId = targetContainerId;
    }

    public Integer getSourceContainerId() {
        return sourceContainerId;
    }

    public void setSourceContainerId(Integer sourceContainerId) {
        this.sourceContainerId = sourceContainerId;
    }
}
