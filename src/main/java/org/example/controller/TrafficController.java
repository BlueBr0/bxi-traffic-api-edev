package org.example.controller;

import org.example.model.SensorResponse;
import org.example.model.TotalResponse;
import org.example.services.TrafficService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/traffic")
public class TrafficController {

    private final TrafficService trafficservice;

    public TrafficController(TrafficService trafficservice){
        this.trafficservice = trafficservice;
    }

    @GetMapping("/total")
    public TotalResponse total(){
        return trafficservice.getTotal();
    }

    @GetMapping("/sensors")
    public List<String> sensors(){
        return trafficservice.getSensorNames();
    }

    @GetMapping("/sensor/{id}")
    public SensorResponse sensor(@PathVariable("id") String id){
        return trafficservice.getSensor(id);
    }
}
