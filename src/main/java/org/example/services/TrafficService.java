package org.example.services;

import org.example.client.BxlMobilityClient;
import org.example.exceptions.SensorNotFoundException;
import org.example.model.SensorReading;
import org.example.model.SensorResponse;
import org.example.model.TotalResponse;
import org.example.model.TrafficSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class TrafficService {

    private static final Logger log = LoggerFactory.getLogger(TrafficService.class);

    public record LatestSnapshot(TrafficSnapshot snapshot, boolean stale) {}

    private final BxlMobilityClient client;
    private final SnapshotParser parser;
    private final String frequency;
    private final Duration duration;

    private TrafficSnapshot trafficSnapshotCached;
    private Instant snapshotCachedAt;
    private boolean stale;

    public TrafficService(BxlMobilityClient client,
                          SnapshotParser parser,
                          @Value("${traffic.frequency:1m}") String frequency,
                          @Value("${traffic.cache-ttl-seconds:30}") long ttlSeconds) {
        this.client = client;
        this.parser = parser;
        this.frequency = frequency;
        this.duration = Duration.ofSeconds(ttlSeconds);
    }

    public synchronized LatestSnapshot getLatestSnapsot(){
        Instant now = Instant.now();
        if(trafficSnapshotCached != null && Duration.between(snapshotCachedAt, now).compareTo(duration) < 0){
            return new LatestSnapshot(trafficSnapshotCached, stale);
        }

        try{
            this.trafficSnapshotCached = parser.parse(client.fetchLatestSnapshotJson());
            stale = false;
        } catch(Exception e){
            if(trafficSnapshotCached == null) {
                throw e;
            }
            log.warn("Utilisation du dernier snapshot connu : {}", e.getMessage());
            stale = true;
        }
        snapshotCachedAt = now;
        return new LatestSnapshot(trafficSnapshotCached, stale);
    }

    public TotalResponse getTotal() {
        LatestSnapshot latestSnapsot = getLatestSnapsot();
        List<SensorReading> sensors = latestSnapsot.snapshot().sensors();
        long total = sensors.stream().mapToLong(s -> s.count(frequency)).sum();
        return new TotalResponse(total, sensors.size(), frequency, latestSnapsot.snapshot().requestDate(), latestSnapsot.stale);
    }

    public List<String> getSensorNames() {
        return getLatestSnapsot().snapshot().sensors().stream()
                .map(SensorReading::name)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public SensorResponse getSensor(String id) {
        LatestSnapshot latest = getLatestSnapsot();
        SensorReading sensor = latest.snapshot().sensors().stream()
                .filter(s -> s.id().equalsIgnoreCase(id) || s.name().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new SensorNotFoundException(id));
        return new SensorResponse(sensor.id(), sensor.name(), sensor.count(frequency), frequency,
                latest.snapshot().requestDate(), latest.stale());
    }
}
