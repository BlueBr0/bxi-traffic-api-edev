package org.example.model;

import java.util.Map;
import java.util.Optional;

public record SensorReading(String id, String name, Map<String, Measurement> byFrequency) {

    public Optional<Measurement> measurement(String frequency) {
        return Optional.ofNullable(byFrequency.get(frequency));
    }

    public long count(String frequency) {
        return measurement(frequency).map(Measurement::count).orElse(0L);
    }
}
