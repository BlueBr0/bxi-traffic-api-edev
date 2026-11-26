package org.example.services;


import org.example.exceptions.SnapshotUnavailableException;
import org.example.model.Measurement;
import org.example.model.SensorReading;
import org.example.model.TrafficSnapshot;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Transformer le JSON de la source en données de classes
 */
@Component
public class SnapshotParser {

    private final JsonMapper mapper;

    public SnapshotParser(JsonMapper mapper) {
        this.mapper = mapper;
    }

    public TrafficSnapshot parse(String json) {
        JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (JacksonException | IllegalArgumentException e) {
            throw new SnapshotUnavailableException("JSON invalide : " + e.getMessage(), e);
        }
        if (root == null || !root.path("data").isObject()) {
            throw new SnapshotUnavailableException("Snapshot inutilisable : objet 'data' absent");
        }

        String requestDate = root.path("requestDate").asString("unknown");
        List<SensorReading> sensors = new ArrayList<>();

        for (Map.Entry<String, JsonNode> sensorEntry : root.get("data").properties()) {
            String id = sensorEntry.getKey();
            JsonNode sensorNode = sensorEntry.getValue();
            String name = sensorNode.path("name").asString(id);

            Map<String, Measurement> byFrequency = new LinkedHashMap<>();
            for (Map.Entry<String, JsonNode> freq : sensorNode.path("results").properties()) {
                Measurement m = readLatest(freq.getValue());
                if (m != null) {
                    byFrequency.put(freq.getKey(), m);
                }
            }
            sensors.add(new SensorReading(id, name, byFrequency));
        }
        return new TrafficSnapshot(requestDate, List.copyOf(sensors), Instant.now());
    }

    private Measurement readLatest(JsonNode frequencyNode) {
        JsonNode latest = frequencyNode.path("t1");
        if (!latest.isObject()) {
            for (Map.Entry<String, JsonNode> entry : frequencyNode.properties()) {
                if (entry.getValue().isObject()) {
                    latest = entry.getValue();
                    break;
                }
            }
        }
        if (!latest.isObject()) {
            return null;
        }
        long count = Math.max(0, latest.path("count").asLong(0));
        double speed = latest.path("speed").asDouble(-1);
        if (speed <= 0) {
            speed = -1;
        }
        double occupancy = Math.max(0, latest.path("occupancy").asDouble(0));
        return new Measurement(count, speed, occupancy,
                firstText(latest, "start_time", "from"), firstText(latest, "end_time", "to"));
    }

    private static String firstText(JsonNode node, String primary, String fallback) {
        JsonNode value = node.path(primary);
        if (value.isMissingNode() || value.isNull()) {
            value = node.path(fallback);
        }
        return (value.isMissingNode() || value.isNull()) ? null : value.asString(null);
    }
}
