package com.example.tp20.car;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CarService {
    private final CarRepository repository;
    private final RestTemplate restTemplate;
    private final String clientUrl;

    public CarService(CarRepository repository, RestTemplate restTemplate,
                      @Value("${clients.base-url}") String clientUrl) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.clientUrl = clientUrl;
    }

    public List<Car> findAll() {
        return repository.findAll().stream().map(this::enrich).toList();
    }

    public Car findById(Long id) {
        return enrich(repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Voiture introuvable : " + id)));
    }

    public Car create(Car input) {
        return repository.save(new Car(null, input.getBrand(), input.getModel(),
                input.getMatricule(), input.getClientId()));
    }

    private Car enrich(Car car) {
        if (car.getClientId() != null) {
            try {
                car.setClient(restTemplate.getForObject(clientUrl + "/{id}", ClientDto.class, car.getClientId()));
            } catch (RestClientException ex) {
                // Le TP garde la voiture disponible même si le service Client est indisponible.
                car.setClient(null);
            }
        }
        return car;
    }
}
