package com.irankiai.backend.Grid;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GridService {

    private final GridRepository gridRepository;
    
    @Autowired
    public GridService(GridRepository gridRepository) {
        this.gridRepository = gridRepository;
    }

    public List<Grid> getAllGrids() {
        return gridRepository.findAll();
    }

    public Optional<Grid> getGrid(Integer id) {
        return gridRepository.findById(id);
    }

    public Grid addGrid(Grid grid) {
        return gridRepository.save(grid);
    }
    
    public void deleteGrid(Integer id) {
        gridRepository.deleteById(id);
    }
    
    public Grid updateGrid(Grid grid) {
        return gridRepository.save(grid);
    }
}