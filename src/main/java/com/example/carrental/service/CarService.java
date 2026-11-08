package com.example.carrental.service;

import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import com.example.carrental.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Basic operations on cars. Goes through CarRepository for all data access.
 */
@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> findAll() {
        // TODO
        throw new UnsupportedOperationException("TODO: findAll");
    }

    public Optional<Car> findById(String id) {
        // TODO
        throw new UnsupportedOperationException("TODO: findById");
    }

    public List<Car> findByType(CarType type) {
        // TODO
        throw new UnsupportedOperationException("TODO: findByType");
    }

    public Car create(Car car) {
        // TODO
        throw new UnsupportedOperationException("TODO: create");
    }

    public Car update(Car car) {
        // TODO
        throw new UnsupportedOperationException("TODO: update");
    }

    public void delete(String id) {
        // TODO
        throw new UnsupportedOperationException("TODO: delete");
    }
}
