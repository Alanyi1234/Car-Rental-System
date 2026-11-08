package com.example.carrental.controller;

import com.example.carrental.domain.NoAvailabilityException;
import com.example.carrental.domain.ReservationCancelledException;
import com.example.carrental.domain.ReservationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Maps domain exceptions to HTTP responses using RFC 9457 ProblemDetail bodies.
 *
 * <p>Extending {@link ResponseEntityExceptionHandler} also turns Spring's own errors
 * (validation failures, unreadable JSON such as an unknown car type, bad query params)
 * into 400 ProblemDetail responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoAvailabilityException.class)
    public ProblemDetail handleNoAvailability(NoAvailabilityException ex) {
        return problem(HttpStatus.CONFLICT, "No availability", ex.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ProblemDetail handleNotFound(ReservationNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Reservation not found", ex.getMessage());
    }

    @ExceptionHandler(ReservationCancelledException.class)
    public ProblemDetail handleCancelled(ReservationCancelledException ex) {
        return problem(HttpStatus.CONFLICT, "Reservation cancelled", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage());
    }

    /** Stubbed endpoints throw this until they are implemented. */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ProblemDetail handleNotImplemented(UnsupportedOperationException ex) {
        return problem(HttpStatus.NOT_IMPLEMENTED, "Not implemented", ex.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
