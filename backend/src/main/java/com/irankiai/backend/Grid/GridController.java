package com.irankiai.backend.Grid;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GridController {

    private final GridService gridService;

    @Autowired
    public GridController(GridService gridService) {
        this.gridService = gridService;
    }

    @GetMapping("/grid")
    public ResponseEntity<Grid> getGrid(@RequestParam Integer id) {
        Optional<Grid> grid = gridService.getGrid(id);
        return grid.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/grids")
    public List<Grid> getAllGrids() {
        return gridService.getAllGrids();
    }

    @PostMapping("/grid")
    public ResponseEntity<Grid> addGrid(@RequestBody Grid grid) {
        Grid savedGrid = gridService.addGrid(grid);
        return new ResponseEntity<>(savedGrid, HttpStatus.CREATED);
    }
    
    @PutMapping("/grid")
    public ResponseEntity<Grid> updateGrid(@RequestBody Grid grid) {
        Grid updatedGrid = gridService.updateGrid(grid);
        return ResponseEntity.ok(updatedGrid);
    }
    
    @DeleteMapping("/grid/{id}")
    public ResponseEntity<Void> deleteGrid(@PathVariable Integer id) {
        gridService.deleteGrid(id);
        return ResponseEntity.noContent().build();
    }
}