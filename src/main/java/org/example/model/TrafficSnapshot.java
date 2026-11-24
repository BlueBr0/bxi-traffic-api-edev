package org.example.model;

import java.time.Instant;
import java.util.List;

public record TrafficSnapshot(String requestDate, List<SensorReading> sensors, Instant fetchedAt) {
}

