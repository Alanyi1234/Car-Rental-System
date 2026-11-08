package com.example.carrental.service;

import com.example.carrental.domain.CarType;
import com.example.carrental.domain.Fleet;
import com.example.carrental.domain.NoAvailabilityException;
import com.example.carrental.domain.Reservation;
import com.example.carrental.repository.InMemoryReservationRepository;
import org.junit.jupiter.api.RepeatedTest;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Many threads race to book the same period. The fleet must never be overbooked.
 */
class RentalServiceConcurrencyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 1, 9, 0);
    private static final LocalDateTime DEC_1 = LocalDateTime.of(2030, 12, 1, 10, 0);
    private static final int SUV_CAPACITY = 3;
    private static final int THREADS = 20;

    @RepeatedTest(5)
    void neverBooksMoreCarsThanExist() throws Exception {
        RentalService service = new RentalService(
                Fleet.withCapacities(Map.of(CarType.SUV, SUV_CAPACITY)),
                new InMemoryReservationRepository(),
                Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startGun = new CountDownLatch(1);
        List<Future<Reservation>> results = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            Callable<Reservation> task = () -> {
                startGun.await();
                try {
                    return service.reserve(CarType.SUV, DEC_1, 3);
                } catch (NoAvailabilityException e) {
                    return null;
                }
            };
            results.add(pool.submit(task));
        }

        startGun.countDown();
        List<Reservation> successes = new ArrayList<>();
        for (Future<Reservation> f : results) {
            Reservation r = f.get(5, TimeUnit.SECONDS);
            if (r != null) {
                successes.add(r);
            }
        }
        pool.shutdown();

        assertThat(successes).hasSize(SUV_CAPACITY);
        assertThat(successes).extracting(r -> r.car().id()).doesNotHaveDuplicates();
    }
}
