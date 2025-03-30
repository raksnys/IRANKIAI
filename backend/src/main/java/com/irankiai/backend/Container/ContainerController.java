package com.irankiai.backend.Container;

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

import com.irankiai.backend.Product.Product;

@RestController
public class ContainerController {

    private final ContainerService containerService;

    @Autowired
    public ContainerController(ContainerService containerService) {
        this.containerService = containerService;
    }

    @GetMapping("/container")
    public ResponseEntity<Container> getContainer(@RequestParam Integer id) {
        Optional<Container> container = containerService.getContainer(id);
        return container.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/containers")
    public List<Container> getAllContainers() {
        return containerService.getAllContainers();
    }

    @PostMapping("/container")
    public ResponseEntity<Container> addContainer(@RequestBody Container container) {
        Container savedContainer = containerService.addContainer(container);
        return new ResponseEntity<>(savedContainer, HttpStatus.CREATED);
    }
    
    @PutMapping("/container")
    public ResponseEntity<Container> updateContainer(@RequestBody Container container) {
        Container updatedContainer = containerService.updateContainer(container);
        return ResponseEntity.ok(updatedContainer);
    }
    
    @DeleteMapping("/container/{id}")
    public ResponseEntity<Void> deleteContainer(@PathVariable Integer id) {
        containerService.deleteContainer(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/container/{id}/product")
    public ResponseEntity<Container> addProductToContainer(
            @PathVariable Integer id, 
            @RequestBody Product product) {
        Container container = containerService.addProductToContainer(id, product);
        if (container != null) {
            return ResponseEntity.ok(container);
        }
        return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/container/{id}/product")
    public ResponseEntity<Container> removeProductFromContainer(
            @PathVariable Integer id, 
            @RequestBody Product product) {
        Container container = containerService.removeProductFromContainer(id, product);
        if (container != null) {
            return ResponseEntity.ok(container);
        }
        return ResponseEntity.notFound().build();
    }
}