package com.example.carrental.config;

import com.example.carrental.domain.CarType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Binds {@code fleet.capacity.*} from application.yml.
 */
@ConfigurationProperties(prefix = "fleet")
public record FleetProperties(Map<CarType, Integer> capacity) {

    public FleetProperties {
        capacity = capacity == null ? Map.of() : Map.copyOf(capacity);
    }
}
