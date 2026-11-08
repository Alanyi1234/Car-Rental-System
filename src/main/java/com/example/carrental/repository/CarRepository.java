package com.example.carrental.repository;

import com.example.carrental.model.Car;
import com.example.carrental.model.CarType;

import java.util.List;

/**
 * Where the fleet's cars are stored and looked up.
 */
public interface CarRepository {

    List<Car> findAll();

    List<Car> findByType(CarType type);
}
