package com.example.carrental.repository;

import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access for the fleet's cars.
 */
@Repository
public class CarRepository {

    public List<Car> findAll() {
        // TODO
        throw new UnsupportedOperationException("TODO: findAll");
    }

    public Optional<Car> findById(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: findById");
    }

    public List<Car> findByType(CarType type) {
        // TODO
        throw new UnsupportedOperationException("TODO: findByType");
    }

    /**
     * Same as {@link #findByType}, but locks the returned rows until the current transaction
     * ends (SELECT ... FOR UPDATE). Used when booking, so two requests can't take the same car.
     */
    public List<Car> findByTypeForUpdate(CarType type) {
        // TODO
        throw new UnsupportedOperationException("TODO: findByTypeForUpdate");
    }

    /** Inserts or updates a car. */
    public void save(Car car) {
        // TODO
        throw new UnsupportedOperationException("TODO: save");
    }

    public void deleteById(UUID id) {
        // TODO
        throw new UnsupportedOperationException("TODO: deleteById");
    }
}
