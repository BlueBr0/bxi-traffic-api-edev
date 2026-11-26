package org.example.services;

import java.util.ArrayList;
import java.util.List;

import org.example.model.Anomaly;
import org.example.model.SensorReading;
import org.example.model.TrafficSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnomalyService {

    private final TrafficService trafficService;
    private final double speedThreshold;
    private final double occupancyThreshold;

    public AnomalyService(TrafficService trafficService,
                          @Value("${anomaly.speed-threshold:30}") double speedThreshold,
                          @Value("${anomaly.occupancy-threshold:40}") double occupancyThreshold) {
        this.trafficService = trafficService;
        this.speedThreshold = speedThreshold;
        this.occupancyThreshold = occupancyThreshold;
    }

    public List<Anomaly> detect() {
        TrafficSnapshot snapshot = trafficService.getLatestSnapsot().snapshot();
        List<Anomaly> anomalies = new ArrayList<>();

        for (SensorReading sensor : snapshot.sensors()) {
            sensor.measurement("1m").ifPresent(m -> {
                if (m.speed() > 0 && m.speed() < speedThreshold && m.occupancy() > occupancyThreshold) {
                    anomalies.add(new Anomaly(sensor.id(), sensor.name(), "CONGESTION",
                            String.format("vitesse %.1f km/h (< %.0f) et occupation %.1f%% (> %.0f)",
                                    m.speed(), speedThreshold, m.occupancy(), occupancyThreshold),
                            snapshot.requestDate()));
                }
            });

            boolean hasLongWindow = sensor.measurement("60m").isPresent();
            boolean allZero = sensor.byFrequency().values().stream().allMatch(m -> m.count() == 0);
            if (hasLongWindow && allZero) {
                anomalies.add(new Anomaly(sensor.id(), sensor.name(), "FLATLINE",
                        "aucun véhicule détecté sur toutes les fenêtres (jusqu'à 60 min)",
                        snapshot.requestDate()));
            }
        }
        return anomalies;
    }
}
