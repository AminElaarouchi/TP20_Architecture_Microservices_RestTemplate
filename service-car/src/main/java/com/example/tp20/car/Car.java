package com.example.tp20.car;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String brand;
    private String model;
    private String matricule;
    private Long clientId;
    @Transient
    private ClientDto client;

    protected Car() { }

    public Car(Long id, String brand, String model, String matricule, Long clientId) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.matricule = matricule;
        this.clientId = clientId;
    }

    public Long getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getMatricule() { return matricule; }
    public Long getClientId() { return clientId; }
    public ClientDto getClient() { return client; }
    public void setClient(ClientDto client) { this.client = client; }
}
