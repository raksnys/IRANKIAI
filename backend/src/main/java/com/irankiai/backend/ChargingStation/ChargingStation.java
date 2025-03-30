package com.irankiai.backend.ChargingStation;

import com.irankiai.backend.Grid.Grid;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "charging_stations")
public class ChargingStation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "grid_id")
    private Grid location;
    
    // NOTE: gal reiktu admin leist valdyt tokias nesamones?
    // NOTE2: pridejau endpoint'us getter setter, jei ka ateiciai turim
    private int chargeRate = 1;
    
    public ChargingStation() {
    }
    
    public ChargingStation(Grid location) {
        this.location = location;
    }
    
    public ChargingStation(Grid location, int chargeRate) {
        this.location = location;
        this.chargeRate = chargeRate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Grid getLocation() {
        return location;
    }

    public void setLocation(Grid location) {
        this.location = location;
    }

    public int getChargeRate() {
        return chargeRate;
    }

    public void setChargeRate(int chargeRate) {
        this.chargeRate = chargeRate;
    }
}