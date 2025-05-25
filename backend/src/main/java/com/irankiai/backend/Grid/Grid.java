package com.irankiai.backend.Grid;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects; // Import Objects

@Entity
@Table(name = "grid", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"x", "y", "z"})
})
public class Grid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int x;
    private int y;
    private int z;

    public Grid() {
    }

    public Grid(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Grid grid = (Grid) o;
        return x == grid.x &&
               y == grid.y &&
               z == grid.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}