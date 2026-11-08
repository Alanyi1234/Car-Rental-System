package com.example.carrental.config;

import com.example.carrental.domain.Fleet;
import com.example.carrental.repository.InMemoryReservationRepository;
import com.example.carrental.repository.ReservationRepository;
import com.example.carrental.service.RentalService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires the plain-Java domain into Spring. The domain and service classes carry no Spring
 * annotations; this is the only place that knows about both.
 */
@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public Fleet fleet(FleetProperties properties) {
        return Fleet.withCapacities(properties.capacity());
    }

    @Bean
    public ReservationRepository reservationRepository() {
        return new InMemoryReservationRepository();
    }

    @Bean
    public RentalService rentalService(Fleet fleet, ReservationRepository repository, Clock clock) {
        return new RentalService(fleet, repository, clock);
    }
}
