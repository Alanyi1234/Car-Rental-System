package com.example.carrental.api;

import com.example.carrental.domain.Car;
import com.example.carrental.domain.CarType;
import com.example.carrental.domain.NoAvailabilityException;
import com.example.carrental.domain.Reservation;
import com.example.carrental.domain.ReservationCancelledException;
import com.example.carrental.domain.ReservationNotFoundException;
import com.example.carrental.service.RentalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-layer tests: request parsing, validation, status codes and response shape.
 * The service is mocked; its behaviour is covered by RentalServiceTest.
 */
@WebMvcTest(controllers = {ReservationController.class, AvailabilityController.class})
class ReservationControllerTest {

    private static final LocalDateTime DEC_1 = LocalDateTime.of(2030, 12, 1, 10, 0);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RentalService rentalService;

    private final Reservation suvReservation = Reservation.create(new Car("SUV-2", CarType.SUV), DEC_1, 3);

    // --- POST /reservations ----------------------------------------------------------------

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(rentalService.reserve(CarType.SUV, DEC_1, 3)).thenReturn(suvReservation);

        mvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType": "SUV", "start": "2030-12-01T10:00:00", "days": 3}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/reservations/" + suvReservation.id()))
                .andExpect(jsonPath("$.id").value(suvReservation.id().toString()))
                .andExpect(jsonPath("$.carId").value("SUV-2"))
                .andExpect(jsonPath("$.carType").value("SUV"))
                .andExpect(jsonPath("$.start").value("2030-12-01T10:00:00"))
                .andExpect(jsonPath("$.end").value("2030-12-04T10:00:00"))
                .andExpect(jsonPath("$.days").value(3))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createReturns409WhenNoCarAvailable() throws Exception {
        when(rentalService.reserve(any(), any(), anyInt()))
                .thenThrow(new NoAvailabilityException(CarType.SUV, DEC_1, DEC_1.plusDays(3)));

        mvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType": "SUV", "start": "2030-12-01T10:00:00", "days": 3}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("No availability"))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createReturns400WhenDaysIsZero() throws Exception {
        mvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType": "SUV", "start": "2030-12-01T10:00:00", "days": 0}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(rentalService);
    }

    @Test
    void createReturns400WhenFieldMissing() throws Exception {
        mvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType": "SUV", "days": 3}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(rentalService);
    }

    @Test
    void createReturns400ForUnknownCarType() throws Exception {
        mvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType": "TRUCK", "start": "2030-12-01T10:00:00", "days": 3}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(rentalService);
    }

    @Test
    void createReturns400WhenServiceRejectsInput() throws Exception {
        when(rentalService.reserve(any(), any(), anyInt()))
                .thenThrow(new IllegalArgumentException("start must not be in the past"));

        mvc.perform(post("/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carType": "SUV", "start": "2020-01-01T10:00:00", "days": 3}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("start must not be in the past"));
    }

    // --- GET /reservations/{id} ------------------------------------------------------------

    @Test
    void getReturnsReservation() throws Exception {
        when(rentalService.getReservation(suvReservation.id())).thenReturn(suvReservation);

        mvc.perform(get("/reservations/{id}", suvReservation.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value("SUV-2"));
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(rentalService.getReservation(id)).thenThrow(new ReservationNotFoundException(id));

        mvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReturns400ForMalformedId() throws Exception {
        mvc.perform(get("/reservations/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    // --- PATCH /reservations/{id} ----------------------------------------------------------

    @Test
    void patchPassesOnlyProvidedFields() throws Exception {
        Reservation extended = suvReservation.reschedule(suvReservation.car(), DEC_1, 5);
        when(rentalService.modify(eq(suvReservation.id()), isNull(), isNull(), eq(5))).thenReturn(extended);

        mvc.perform(patch("/reservations/{id}", suvReservation.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"days": 5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days").value(5))
                .andExpect(jsonPath("$.end").value("2030-12-06T10:00:00"));

        verify(rentalService).modify(suvReservation.id(), null, null, 5);
    }

    @Test
    void patchReturns409WhenNewPeriodUnavailable() throws Exception {
        when(rentalService.modify(any(), any(), any(), any()))
                .thenThrow(new NoAvailabilityException(CarType.SUV, DEC_1, DEC_1.plusDays(5)));

        mvc.perform(patch("/reservations/{id}", suvReservation.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"days": 5}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void patchReturns409WhenReservationCancelled() throws Exception {
        when(rentalService.modify(any(), any(), any(), any()))
                .thenThrow(new ReservationCancelledException(suvReservation.id()));

        mvc.perform(patch("/reservations/{id}", suvReservation.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"days": 5}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Reservation cancelled"));
    }

    @Test
    void patchReturns400ForInvalidDays() throws Exception {
        mvc.perform(patch("/reservations/{id}", suvReservation.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"days": 0}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(rentalService);
    }

    // --- DELETE /reservations/{id} ---------------------------------------------------------

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/reservations/{id}", suvReservation.id()))
                .andExpect(status().isNoContent());

        verify(rentalService).cancel(suvReservation.id());
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ReservationNotFoundException(id)).when(rentalService).cancel(id);

        mvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNotFound());
    }

    // --- GET /availability -----------------------------------------------------------------

    @Test
    void availabilityReturnsCountsForEveryType() throws Exception {
        Map<CarType, Integer> counts = new EnumMap<>(Map.of(CarType.SEDAN, 3, CarType.SUV, 2, CarType.VAN, 0));
        when(rentalService.availability(DEC_1, 3)).thenReturn(counts);

        mvc.perform(get("/availability")
                        .param("start", "2030-12-01T10:00:00")
                        .param("days", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.end").value("2030-12-04T10:00:00"))
                .andExpect(jsonPath("$.available.length()").value(3))
                .andExpect(jsonPath("$.available[?(@.carType == 'VAN')].count").value(0));
    }

    @Test
    void availabilityCanFilterByType() throws Exception {
        Map<CarType, Integer> counts = new EnumMap<>(Map.of(CarType.SEDAN, 3, CarType.SUV, 2, CarType.VAN, 0));
        when(rentalService.availability(DEC_1, 3)).thenReturn(counts);

        mvc.perform(get("/availability")
                        .param("start", "2030-12-01T10:00:00")
                        .param("days", "3")
                        .param("carType", "SUV"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available.length()").value(1))
                .andExpect(jsonPath("$.available[0].carType").value("SUV"))
                .andExpect(jsonPath("$.available[0].count").value(2));
    }

    @Test
    void availabilityReturns400WhenStartMissing() throws Exception {
        mvc.perform(get("/availability").param("days", "3"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void availabilityReturns400ForUnknownCarType() throws Exception {
        mvc.perform(get("/availability")
                        .param("start", "2030-12-01T10:00:00")
                        .param("days", "3")
                        .param("carType", "TRUCK"))
                .andExpect(status().isBadRequest());
    }
}
