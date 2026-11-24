package org.example.model;

public record TotalResponse
        (
                long total,
                int sensorCount,
                String frequency,
                String requestDate,
                boolean stale
        ) {}
