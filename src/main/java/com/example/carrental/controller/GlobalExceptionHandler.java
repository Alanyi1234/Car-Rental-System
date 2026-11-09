package com.example.carrental.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.NoSuchElementException;

/**
 * Turns exceptions from any controller into HTTP error responses. Spring applies this to every
 * controller automatically, so the controllers don't need their own handlers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400 Bad Request: the client sent invalid input. */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,          // body field fails validation, e.g. missing carType or days < 1
            HttpMessageNotReadableException.class,          // body isn't valid JSON, or has an unknown value like "TRUCK"
            MissingServletRequestParameterException.class,  // required query param missing, e.g. no start
            MethodArgumentTypeMismatchException.class,      // param has the wrong type, e.g. id isn't a UUID
            IllegalArgumentException.class                  // service rule broken: start in the past or days < 1
    })
    public ResponseEntity<String> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid request: " + ex.getMessage());
    }

    /** 404 Not Found: no reservation with that id. */
    @ExceptionHandler(NoSuchElementException.class)  // thrown by RentalService.getReservation
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /** 409 Conflict: the request is valid but can't be done right now. */
    @ExceptionHandler({
            IllegalStateException.class,           // no car free, or reservation is cancelled
            DataIntegrityViolationException.class  // DB constraint rejected an overlapping booking
    })
    public ResponseEntity<String> handleConflict(Exception ex) {
        String message = ex instanceof DataIntegrityViolationException
                ? "That car was just booked by someone else"
                : ex.getMessage();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    /** 500 Internal Server Error: anything not handled above. */
    @ExceptionHandler(Exception.class)  // catch-all for unexpected errors
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong: " + ex.getMessage());
    }
}
