package com.irankiai.backend.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    // FIXME: Reiktu padaryt actual dimensions modeli, kad normaliai handlintu.
    private String dimensions;
    private float weight;
    private float price;
    // FIXME: Virsutinis komentaras ta pati pasako. 
    private String color;
    
    // IMPROVE: kategorijos, nuotrauku url, aprasymas, kiekis, prekes kodas
    // visai reiktu, ne? :D

    public Product() {
    }

    public Product(int id, String name, String dimensions, float weight, float price, String color) {
        this.id = id;
        this.name = name;
        this.dimensions = dimensions;
        this.weight = weight;
        this.price = price;
        this.color = color;
    }

    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDimensions() {
        return dimensions;
    }
    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
    public float getWeight() {
        return weight;
    }
    public void setWeight(float weight) {
        this.weight = weight;
    }
    public float getPrice() {
        return price;
    }
    public void setPrice(float price) {
        this.price = price;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }

}
