package com.example.carrental.repository;

import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InMemoryCarRepository implements CarRepository {

    @Override
    public List<Car> findAll() {
        // TODO
        throw new UnsupportedOperationException("TODO: findAll");
    }

    @Override
    public List<Car> findByType(CarType type) {
        // TODO
        throw new UnsupportedOperationException("TODO: findByType");
    }
}
