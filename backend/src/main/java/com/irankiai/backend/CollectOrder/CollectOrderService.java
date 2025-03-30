package com.irankiai.backend.CollectOrder;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;

@Service
public class CollectOrderService {

    private final CollectOrderRepository collectOrderRepository;
    private final ContainerRepository containerRepository;
    
    @Autowired
    public CollectOrderService(CollectOrderRepository collectOrderRepository, ContainerRepository containerRepository) {
        this.collectOrderRepository = collectOrderRepository;
        this.containerRepository = containerRepository;
    }

    public List<CollectOrder> getAllCollectOrders() {
        return collectOrderRepository.findAll();
    }

    public Optional<CollectOrder> getCollectOrder(Integer id) {
        return collectOrderRepository.findById(id);
    }

    public CollectOrder addCollectOrder(CollectOrder collectOrder) {
        return collectOrderRepository.save(collectOrder);
    }
    
    public void deleteCollectOrder(Integer id) {
        collectOrderRepository.deleteById(id);
    }
    
    public CollectOrder updateCollectOrder(CollectOrder collectOrder) {
        return collectOrderRepository.save(collectOrder);
    }
    
    public CollectOrder setContainer(Integer collectOrderId, Integer containerId) {
        Optional<CollectOrder> collectOrderOpt = collectOrderRepository.findById(collectOrderId);
        Optional<Container> containerOpt = containerRepository.findById(containerId);
        
        if (collectOrderOpt.isPresent() && containerOpt.isPresent()) {
            CollectOrder collectOrder = collectOrderOpt.get();
            Container container = containerOpt.get();
            
            // Update container location to match collect order location
            container.setLocation(collectOrder.getLocation());
            containerRepository.save(container);
            
            // Set container on collect order
            collectOrder.setContainer(container);
            return collectOrderRepository.save(collectOrder);
        }
        return null;
    }
    
    public CollectOrder removeContainer(Integer collectOrderId) {
        Optional<CollectOrder> collectOrderOpt = collectOrderRepository.findById(collectOrderId);
        
        if (collectOrderOpt.isPresent()) {
            CollectOrder collectOrder = collectOrderOpt.get();
            
            // Remove container reference from collect order
            collectOrder.setContainer(null);
            return collectOrderRepository.save(collectOrder);
        }
        return null;
    }
}