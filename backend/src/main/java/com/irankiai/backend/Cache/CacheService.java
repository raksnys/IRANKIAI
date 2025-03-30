package com.irankiai.backend.Cache;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.irankiai.backend.Container.Container;
import com.irankiai.backend.Container.ContainerRepository;

@Service
public class CacheService {

    private final CacheRepository cacheRepository;
    private final ContainerRepository containerRepository;
    
    @Autowired
    public CacheService(CacheRepository cacheRepository, ContainerRepository containerRepository) {
        this.cacheRepository = cacheRepository;
        this.containerRepository = containerRepository;
    }

    public List<Cache> getAllCaches() {
        return cacheRepository.findAll();
    }

    public Optional<Cache> getCache(Integer id) {
        return cacheRepository.findById(id);
    }

    public Cache addCache(Cache cache) {
        return cacheRepository.save(cache);
    }
    
    public void deleteCache(Integer id) {
        cacheRepository.deleteById(id);
    }
    
    public Cache updateCache(Cache cache) {
        return cacheRepository.save(cache);
    }
    
    public Cache storeContainer(Integer cacheId, Integer containerId) {
        Optional<Cache> cacheOpt = cacheRepository.findById(cacheId);
        Optional<Container> containerOpt = containerRepository.findById(containerId);
        
        if (cacheOpt.isPresent() && containerOpt.isPresent()) {
            Cache cache = cacheOpt.get();
            Container container = containerOpt.get();
            
            // Check if cache already has a container
            if (cache.hasContainer()) {
                return null; // Cache is already occupied
            }
            
            // Update container location to cache location
            container.setLocation(cache.getLocation());
            containerRepository.save(container);
            
            // Store container in cache
            cache.setContainer(container);
            return cacheRepository.save(cache);
        }
        return null;
    }
    
    public Cache removeContainer(Integer cacheId) {
        Optional<Cache> cacheOpt = cacheRepository.findById(cacheId);
        
        if (cacheOpt.isPresent()) {
            Cache cache = cacheOpt.get();
            
            // Check if cache has a container
            if (!cache.hasContainer()) {
                return cache; // Nothing to remove
            }
            
            // Remove container from cache
            cache.setContainer(null);
            return cacheRepository.save(cache);
        }
        return null;
    }
}