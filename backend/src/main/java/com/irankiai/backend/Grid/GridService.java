package com.irankiai.backend.Grid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;

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

    @Autowired
    private ContainerRepository containerRepository;

    public List<GridDTO> getGridByType(String type) {
        List<GridDTO> result = new ArrayList<>();

        if (type == null || type.isEmpty()) {
            addAllGridTypes(result);
        } else {
            switch (type) {
                case "CONTAINER":
                    addContainersToGrid(result);
                    break;
                case "ROBOT":
                    addRobotsToGrid(result);
                    break;
                case "CHARGING_STATION":
                    addChargingStationsToGrid(result);
                    break;
            }
        }

        return result;
    }

    private void addAllGridTypes(List<GridDTO> result) {
        addContainersToGrid(result);
        addRobotsToGrid(result);
        addChargingStationsToGrid(result);
    }

    private void addContainersToGrid(List<GridDTO> result) {
        List<Container> containers = containerRepository.findAll();
        for (Container container : containers) {
            Grid grid = container.getLocation();
            result.add(new GridDTO(grid.getX(), grid.getY(), grid.getZ(), "CONTAINER"));
        }
    }

    // TODO: Implementuot robotus paimt i grid
    private void addRobotsToGrid(List<GridDTO> result) {
    }

    // TODO: Implemenetuot krovimo stoteles i grid
    private void addChargingStationsToGrid(List<GridDTO> result) {
    }
}