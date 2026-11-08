package com.example.carrental.repository;

import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access for the fleet's cars.
 */
@Repository
public class CarRepository {

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

    /** Inserts or updates a car. */
    public void save(Car car) {
        // TODO
        throw new UnsupportedOperationException("TODO: save");
    }

    public void deleteById(String id) {
        // TODO
        throw new UnsupportedOperationException("TODO: deleteById");
    }
}
