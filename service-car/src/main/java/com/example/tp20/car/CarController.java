package com.example.tp20.car;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/car")
public class CarController {
    private final CarService service;

    public CarController(CarService service) { this.service = service; }

    @GetMapping
    public List<Car> findAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public Car findById(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<Car> create(@RequestBody Car input) {
        Car saved = service.create(input);
        return ResponseEntity.created(URI.create("/api/car/" + saved.getId())).body(saved);
    }
}
