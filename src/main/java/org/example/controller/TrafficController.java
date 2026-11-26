package org.example.controller;

import org.example.model.Anomaly;
import org.example.model.SensorResponse;
import org.example.model.TotalResponse;
import org.example.services.AnomalyService;
import org.example.services.TrafficService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private final TrafficService trafficService;
    private final AnomalyService anomalyService;

    public TrafficController(TrafficService trafficService, AnomalyService anomalyService) {
        this.trafficService = trafficService;
        this.anomalyService = anomalyService;
    }

    @GetMapping("/total")
    public TotalResponse total(){
        return trafficService.getTotal();
    }

    @GetMapping("/sensors")
    public List<String> sensors(){
        return trafficService.getSensorNames();
    }

    @GetMapping("/sensor/{id}")
    public SensorResponse sensor(@PathVariable("id") String id){
        return trafficService.getSensor(id);
    }

    @GetMapping("/anomalies")
    public List<Anomaly> anomalies() {
        return anomalyService.detect();
    }

}
