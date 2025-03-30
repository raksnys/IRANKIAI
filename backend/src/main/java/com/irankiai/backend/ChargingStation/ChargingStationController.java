package com.irankiai.backend.ChargingStation;

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
public class ChargingStationController {

    private final ChargingStationService chargingStationService;

    @Autowired
    public ChargingStationController(ChargingStationService chargingStationService) {
        this.chargingStationService = chargingStationService;
    }

    @GetMapping("/chargingStation")
    public ResponseEntity<ChargingStation> getChargingStation(@RequestParam Integer id) {
        Optional<ChargingStation> station = chargingStationService.getChargingStation(id);
        return station.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/chargingStations")
    public List<ChargingStation> getAllChargingStations() {
        return chargingStationService.getAllChargingStations();
    }

    @PostMapping("/chargingStation")
    public ResponseEntity<ChargingStation> addChargingStation(@RequestBody ChargingStation chargingStation) {
        ChargingStation savedStation = chargingStationService.addChargingStation(chargingStation);
        return new ResponseEntity<>(savedStation, HttpStatus.CREATED);
    }
    
    @PutMapping("/chargingStation")
    public ResponseEntity<ChargingStation> updateChargingStation(@RequestBody ChargingStation chargingStation) {
        ChargingStation updatedStation = chargingStationService.updateChargingStation(chargingStation);
        return ResponseEntity.ok(updatedStation);
    }
    
    @DeleteMapping("/chargingStation/{id}")
    public ResponseEntity<Void> deleteChargingStation(@PathVariable Integer id) {
        chargingStationService.deleteChargingStation(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/chargingStation/{id}/chargeRate")
    public ResponseEntity<ChargingStation> setChargeRate(
            @PathVariable Integer id,
            @RequestParam int rate) {
        ChargingStation station = chargingStationService.setChargeRate(id, rate);
        if (station != null) {
            return ResponseEntity.ok(station);
        }
        return ResponseEntity.notFound().build();
    }
}