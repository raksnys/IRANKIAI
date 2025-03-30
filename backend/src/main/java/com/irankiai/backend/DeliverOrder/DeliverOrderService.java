package com.irankiai.backend.DeliverOrder;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;

@Service
public class DeliverOrderService {

    private final DeliverOrderRepository deliverOrderRepository;
    private final ContainerRepository containerRepository;
    
    @Autowired
    public DeliverOrderService(DeliverOrderRepository deliverOrderRepository, ContainerRepository containerRepository) {
        this.deliverOrderRepository = deliverOrderRepository;
        this.containerRepository = containerRepository;
    }

    public List<DeliverOrder> getAllDeliverOrders() {
        return deliverOrderRepository.findAll();
    }

    public Optional<DeliverOrder> getDeliverOrder(Integer id) {
        return deliverOrderRepository.findById(id);
    }

    public DeliverOrder addDeliverOrder(DeliverOrder deliverOrder) {
        return deliverOrderRepository.save(deliverOrder);
    }
    
    public void deleteDeliverOrder(Integer id) {
        deliverOrderRepository.deleteById(id);
    }
    
    public DeliverOrder updateDeliverOrder(DeliverOrder deliverOrder) {
        return deliverOrderRepository.save(deliverOrder);
    }
    
    public DeliverOrder setContainer(Integer deliverOrderId, Container container) {
        Optional<DeliverOrder> deliverOrderOpt = deliverOrderRepository.findById(deliverOrderId);
        
        if (deliverOrderOpt.isPresent()) {
            DeliverOrder deliverOrder = deliverOrderOpt.get();
            
            // Check if deliver order already has a container
            if (deliverOrder.hasContainer()) {
                return null; // Already has a container
            }
            
            // Update container location to match deliver order location
            container.setLocation(deliverOrder.getLocation());
            containerRepository.save(container);
            
            // Set container on deliver order
            deliverOrder.setContainer(container);
            return deliverOrderRepository.save(deliverOrder);
        }
        return null;
    }
}