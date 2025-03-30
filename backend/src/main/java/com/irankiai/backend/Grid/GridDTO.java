package com.irankiai.backend.Grid;

public class GridDTO {
    private int x;
    private int y;
    private int z;
    private String type;
    private int entityId;

    public GridDTO() {
    }

    public GridDTO(int x, int y, int z, String type, int entityId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.entityId = entityId;
    }

    // Getters and setters
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getEntityId() { return entityId; }
    public void setEntityId(int entityId) { this.entityId = entityId; }
}