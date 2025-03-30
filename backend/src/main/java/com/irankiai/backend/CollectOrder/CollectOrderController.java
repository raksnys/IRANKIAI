package com.irankiai.backend.CollectOrder;

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
public class CollectOrderController {

    private final CollectOrderService collectOrderService;

    @Autowired
    public CollectOrderController(CollectOrderService collectOrderService) {
        this.collectOrderService = collectOrderService;
    }

    @GetMapping("/collectOrder")
    public ResponseEntity<CollectOrder> getCollectOrder(@RequestParam Integer id) {
        Optional<CollectOrder> collectOrder = collectOrderService.getCollectOrder(id);
        return collectOrder.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/collectOrders")
    public List<CollectOrder> getAllCollectOrders() {
        return collectOrderService.getAllCollectOrders();
    }

    @PostMapping("/collectOrder")
    public ResponseEntity<CollectOrder> addCollectOrder(@RequestBody CollectOrder collectOrder) {
        CollectOrder savedCollectOrder = collectOrderService.addCollectOrder(collectOrder);
        return new ResponseEntity<>(savedCollectOrder, HttpStatus.CREATED);
    }
    
    @PutMapping("/collectOrder")
    public ResponseEntity<CollectOrder> updateCollectOrder(@RequestBody CollectOrder collectOrder) {
        CollectOrder updatedCollectOrder = collectOrderService.updateCollectOrder(collectOrder);
        return ResponseEntity.ok(updatedCollectOrder);
    }
    
    @DeleteMapping("/collectOrder/{id}")
    public ResponseEntity<Void> deleteCollectOrder(@PathVariable Integer id) {
        collectOrderService.deleteCollectOrder(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/collectOrder/{collectOrderId}/container/{containerId}")
    public ResponseEntity<CollectOrder> setContainer(
            @PathVariable Integer collectOrderId,
            @PathVariable Integer containerId) {
        CollectOrder collectOrder = collectOrderService.setContainer(collectOrderId, containerId);
        if (collectOrder != null) {
            return ResponseEntity.ok(collectOrder);
        }
        return ResponseEntity.badRequest().build();
    }
}