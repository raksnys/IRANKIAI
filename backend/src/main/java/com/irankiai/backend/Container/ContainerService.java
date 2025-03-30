package com.irankiai.backend.Container;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.irankiai.backend.Product.Product;

@Service
public class ContainerService {

    private final ContainerRepository containerRepository;
    
    @Autowired
    public ContainerService(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    public List<Container> getAllContainers() {
        return containerRepository.findAll();
    }

    public Optional<Container> getContainer(Integer id) {
        return containerRepository.findById(id);
    }

    public Container addContainer(Container container) {
        return containerRepository.save(container);
    }
    
    public void deleteContainer(Integer id) {
        containerRepository.deleteById(id);
    }
    
    public Container updateContainer(Container container) {
        return containerRepository.save(container);
    }
    
    public Container addProductToContainer(Integer containerId, Product product) {
        Optional<Container> containerOpt = containerRepository.findById(containerId);
        if (containerOpt.isPresent()) {
            Container container = containerOpt.get();
            container.addProduct(product);
            return containerRepository.save(container);
        }
        return null;
    }
    
    public Container removeProductFromContainer(Integer containerId, Product product) {
        Optional<Container> containerOpt = containerRepository.findById(containerId);
        if (containerOpt.isPresent()) {
            Container container = containerOpt.get();
            container.removeProduct(product);
            return containerRepository.save(container);
        }
        return null;
    }
}