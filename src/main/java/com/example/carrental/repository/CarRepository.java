package com.example.carrental.repository;

import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for the fleet's cars.
 */
@Repository
public class CarRepository {

    public List<Car> findAll() {
        // TODO
        throw new UnsupportedOperationException("TODO: findAll");
    }

    public List<Car> findByType(CarType type) {
        // TODO
        throw new UnsupportedOperationException("TODO: findByType");
    }
}
