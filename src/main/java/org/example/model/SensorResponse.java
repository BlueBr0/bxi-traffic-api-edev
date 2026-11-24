package org.example.model;

public record SensorResponse
        (
                String id,
                String name,
                long count,
                String frequency,
                String requestDate,
                boolean stale
        ) {}
