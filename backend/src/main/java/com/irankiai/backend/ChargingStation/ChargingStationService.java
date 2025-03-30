package com.irankiai.backend.ChargingStation;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargingStationService {

    private final ChargingStationRepository chargingStationRepository;
    
    @Autowired
    public ChargingStationService(ChargingStationRepository chargingStationRepository) {
        this.chargingStationRepository = chargingStationRepository;
    }

    public List<ChargingStation> getAllChargingStations() {
        return chargingStationRepository.findAll();
    }

    public Optional<ChargingStation> getChargingStation(Integer id) {
        return chargingStationRepository.findById(id);
    }

    public ChargingStation addChargingStation(ChargingStation chargingStation) {
        return chargingStationRepository.save(chargingStation);
    }
    
    public void deleteChargingStation(Integer id) {
        chargingStationRepository.deleteById(id);
    }
    
    public ChargingStation updateChargingStation(ChargingStation chargingStation) {
        return chargingStationRepository.save(chargingStation);
    }
    
    public ChargingStation setChargeRate(Integer id, int chargeRate) {
        Optional<ChargingStation> stationOpt = chargingStationRepository.findById(id);
        if (stationOpt.isPresent()) {
            ChargingStation station = stationOpt.get();
            station.setChargeRate(chargeRate);
            return chargingStationRepository.save(station);
        }
        return null;
    }
}