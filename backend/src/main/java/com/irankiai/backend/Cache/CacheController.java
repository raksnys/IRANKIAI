package com.irankiai.backend.Cache;

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
public class CacheController {

    private final CacheService cacheService;

    @Autowired
    public CacheController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @GetMapping("/cache")
    public ResponseEntity<Cache> getCache(@RequestParam Integer id) {
        Optional<Cache> cache = cacheService.getCache(id);
        return cache.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/caches")
    public List<Cache> getAllCaches() {
        return cacheService.getAllCaches();
    }

    @PostMapping("/cache")
    public ResponseEntity<Cache> addCache(@RequestBody Cache cache) {
        Cache savedCache = cacheService.addCache(cache);
        return new ResponseEntity<>(savedCache, HttpStatus.CREATED);
    }

    @PutMapping("/cache")
    public ResponseEntity<Cache> updateCache(@RequestBody Cache cache) {
        Cache updatedCache = cacheService.updateCache(cache);
        return ResponseEntity.ok(updatedCache);
    }

    @DeleteMapping("/cache/{id}")
    public ResponseEntity<Void> deleteCache(@PathVariable Integer id) {
        cacheService.deleteCache(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cache/{cacheId}/container/{containerId}")
    public ResponseEntity<Cache> storeContainer(
            @PathVariable Integer cacheId,
            @PathVariable Integer containerId) {
        Cache cache = cacheService.storeContainer(cacheId, containerId);
        if (cache != null) {
            return ResponseEntity.ok(cache);
        }
        return ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/cache/{cacheId}/container")
    public ResponseEntity<Cache> removeContainer(@PathVariable Integer cacheId) {
        Cache cache = cacheService.removeContainer(cacheId);
        if (cache != null) {
            return ResponseEntity.ok(cache);
        }
        return ResponseEntity.notFound().build();
    }
}