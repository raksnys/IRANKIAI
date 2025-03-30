package com.irankiai.backend.DeliverOrder;

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
public class DeliverOrderController {

    private final DeliverOrderService deliverOrderService;

    @Autowired
    public DeliverOrderController(DeliverOrderService deliverOrderService) {
        this.deliverOrderService = deliverOrderService;
    }

    @GetMapping("/deliverOrder")
    public ResponseEntity<DeliverOrder> getDeliverOrder(@RequestParam Integer id) {
        Optional<DeliverOrder> deliverOrder = deliverOrderService.getDeliverOrder(id);
        return deliverOrder.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/deliverOrders")
    public List<DeliverOrder> getAllDeliverOrders() {
        return deliverOrderService.getAllDeliverOrders();
    }

    @PostMapping("/deliverOrder")
    public ResponseEntity<DeliverOrder> addDeliverOrder(@RequestBody DeliverOrder deliverOrder) {
        DeliverOrder savedDeliverOrder = deliverOrderService.addDeliverOrder(deliverOrder);
        return new ResponseEntity<>(savedDeliverOrder, HttpStatus.CREATED);
    }
    
    @PutMapping("/deliverOrder")
    public ResponseEntity<DeliverOrder> updateDeliverOrder(@RequestBody DeliverOrder deliverOrder) {
        DeliverOrder updatedDeliverOrder = deliverOrderService.updateDeliverOrder(deliverOrder);
        return ResponseEntity.ok(updatedDeliverOrder);
    }
    
    @DeleteMapping("/deliverOrder/{id}")
    public ResponseEntity<Void> deleteDeliverOrder(@PathVariable Integer id) {
        deliverOrderService.deleteDeliverOrder(id);
        return ResponseEntity.noContent().build();
    }
}