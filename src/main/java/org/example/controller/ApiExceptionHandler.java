package org.example.controller;

import java.util.Map;

import org.example.exceptions.SensorNotFoundException;
import org.example.exceptions.SnapshotUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(SensorNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(SensorNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SnapshotUnavailableException.class)
    public ResponseEntity<Map<String, String>> unavailable(SnapshotUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
    }
}
